import logging
import mysql.connector
import pandas as pd
import numpy as np
from datetime import datetime, timedelta
from holidays import China
from sklearn.preprocessing import MinMaxScaler, OneHotEncoder
from sklearn.gaussian_process import GaussianProcessRegressor
from sklearn.gaussian_process.kernels import RBF, WhiteKernel
import re
import warnings
import requests
warnings.filterwarnings("ignore")

# 配置日志
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s',
    handlers=[
        logging.FileHandler('passenger_flow_prediction.log'),
        logging.StreamHandler()
    ]
)
logger = logging.getLogger(__name__)

# 数据库连接配置
DB_CONFIG = {
    'host': 'localhost',
    'port': 3306,
    'user': 'root',
    'password': 'root',
    'database': 'hzibus_dm_dev'
}

# 通道与 route_name 映射
CHANNEL_MAPPING = {
    'C1': ['黄龙体育中心至灵隐专线', '武林广场至灵隐专线'],
    'C2': ['278M路', '319M路', '西溪路停车场至灵隐接驳线'],
    'C3': ['505路'],
    'C4': ['7路'],
    'C5': ['龙翔桥至灵隐专线']
}

# 模拟天气数据
def generate_simulated_weather_data(start_date, end_date):
    logger.info("生成模拟天气数据...")
    dates = pd.date_range(start=start_date, end=end_date, freq='15min')
    np.random.seed(42)
    weather_data = {
        'datetime': dates,
        'temperature': np.random.normal(25, 5, len(dates)),
        'precipitation': np.random.exponential(0.5, len(dates)),
        'wind_speed': np.random.uniform(0, 10, len(dates)),
        'weather_type': np.random.choice(['sunny', 'cloudy', 'rainy'], len(dates), p=[0.5, 0.3, 0.2])
    }
    weather_df = pd.DataFrame(weather_data)
    weather_df['temperature'] = np.clip(weather_df['temperature'], -20, 45)
    weather_df['precipitation'] = np.clip(weather_df['precipitation'], 0, 100)
    weather_df['wind_speed'] = np.clip(weather_df['wind_speed'], 0, 30)
    logger.info("模拟天气数据生成完成，记录数: %d", len(weather_df))
    return weather_df

# 实时天气API接口
def fetch_real_time_weather(start_date, end_date):
    logger.info("从 Open-Meteo 获取实时天气数据...")
    url = "https://api.open-meteo.com/v1/forecast"
    params = {
        "latitude": 30.27,  # 杭州纬度
        "longitude": 120.15,  # 杭州经度
        "hourly": "temperature_2m,precipitation,wind_speed_10m,weathercode",
        "start_date": start_date,
        "end_date": end_date
    }
    try:
        response = requests.get(url, params=params)
        response.raise_for_status()
        data = response.json()
        weather_df = pd.DataFrame({
            'datetime': pd.to_datetime(data['hourly']['time']),
            'temperature': data['hourly']['temperature_2m'],
            'precipitation': data['hourly']['precipitation'],
            'wind_speed': data['hourly']['wind_speed_10m'],
            'weather_type': [map_weather_code(code) for code in data['hourly']['weathercode']]
        })
        logger.info("实时天气数据获取完成，记录数: %d", len(weather_df))
        return weather_df
    except Exception as e:
        logger.error("获取天气数据失败: %s", str(e))
        logger.warning("使用模拟数据作为后备")
        return generate_simulated_weather_data(start_date, end_date)

def map_weather_code(code):
    # WMO 天气代码映射到类型
    if code in [0, 1]: return 'sunny'
    elif code in [2, 3]: return 'cloudy'
    elif code in [51, 53, 55, 61, 63, 65, 80, 81, 82]: return 'rainy'
    return 'cloudy'  # 默认

# 数据提取
def fetch_data_from_db():
    logger.info("开始从数据库提取数据...")
    try:
        conn = mysql.connector.connect(**DB_CONFIG)
        cursor = conn.cursor(dictionary=True)

        query_trade = """
        SELECT trade_time, stop_id, off_stop_id, route_id, direction, route_name
        FROM t_dm_trade_zhipu
        WHERE stop_id = '1001001154' OR off_stop_id = '1001001154'
        AND trade_time >= '2025-06-01 00:00:00' AND trade_time < '2025-07-01 00:00:00'
        """
        cursor.execute(query_trade)
        trade_data = pd.DataFrame(cursor.fetchall())
        logger.info("提取交易数据记录数: %d", len(trade_data))

        query_broadcast = """
        SELECT arrive_time, leave_time, board_amount, off_amount, on_bus_amount, stop_id, route_name
        FROM t_dm_sim_station_broacast_zhipu
        WHERE stop_id = '1001001154'
        AND arrive_time >= '2025-06-01 00:00:00' AND arrive_time < '2025-07-01 00:00:00'
        """
        cursor.execute(query_broadcast)
        broadcast_data = pd.DataFrame(cursor.fetchall())
        logger.info("提取报站数据记录数: %d", len(broadcast_data))

        query_schedule = """
        SELECT assign_name, dispatch_departure_time, dispatch_end_time, single_trip_duration, terminal_id, route_name
        FROM t_dm_dispatch_assign_schedule
        WHERE origin_id = '1001001154' OR terminal_id = '1001001154'
        """
        cursor.execute(query_schedule)
        schedule_data = pd.DataFrame(cursor.fetchall())
        logger.info("提取调度数据记录数: %d", len(schedule_data))
        logger.info("调度数据原始样例: %s", schedule_data[['assign_name', 'dispatch_departure_time', 'dispatch_end_time', 'route_name']].head().to_dict())

        cursor.close()
        conn.close()
        return trade_data, broadcast_data, schedule_data
    except Exception as e:
        logger.error("数据库查询失败: %s", str(e))
        raise

# 调度数据日期拼接并映射通道
def process_schedule_dates(schedule_data):
    logger.info("开始处理调度数据日期...")
    # 从 assign_name 提取日期
    def extract_date(assign_name):
        if pd.isna(assign_name):
            logger.warning("assign_name 为空，使用默认日期 2025-06-01")
            return '2025-06-01'
        match = re.search(r'(\d{4}-\d{2}-\d{2})', assign_name)
        return match.group(1) if match else '2025-06-01'

    schedule_data['date'] = schedule_data['assign_name'].apply(extract_date)

    # 将 Timedelta 转换为 HH:MM:SS 格式的字符串
    def timedelta_to_time_str(td):
        if pd.isna(td):
            logger.warning("时间值为空，使用默认时间 00:00:00")
            return '00:00:00'
        total_seconds = int(td.total_seconds())
        hours, remainder = divmod(total_seconds, 3600)
        minutes, seconds = divmod(remainder, 60)
        return f'{hours:02d}:{minutes:02d}:{seconds:02d}'

    schedule_data['departure_time_str'] = schedule_data['dispatch_departure_time'].apply(timedelta_to_time_str)
    schedule_data['end_time_str'] = schedule_data['dispatch_end_time'].apply(timedelta_to_time_str)

    # 记录时间字符串
    logger.info("时间字符串样例: %s", schedule_data[['dispatch_departure_time', 'departure_time_str', 'dispatch_end_time', 'end_time_str']].head().to_dict())

    # 拼接日期和时间
    schedule_data['plan_departure_time'] = pd.to_datetime(
        schedule_data['date'] + ' ' + schedule_data['departure_time_str'],
        format='%Y-%m-%d %H:%M:%S',
        errors='coerce'
    )
    schedule_data['plan_end_time'] = pd.to_datetime(
        schedule_data['date'] + ' ' + schedule_data['end_time_str'],
        format='%Y-%m-%d %H:%M:%S',
        errors='coerce'
    )

    # 过滤时间范围
    start_date = pd.to_datetime('2025-06-01 00:00:00')
    end_date = pd.to_datetime('2025-07-01 00:00:00')
    schedule_data = schedule_data[
        (schedule_data['plan_departure_time'] >= start_date) &
        (schedule_data['plan_departure_time'] < end_date)
        ]

    # 确保 single_trip_duration 有效
    schedule_data['single_trip_duration'] = np.clip(schedule_data['single_trip_duration'], 5, 60)
    schedule_data['single_trip_duration'] = schedule_data['single_trip_duration'].fillna(10)

    # 删除 plan_departure_time 无效的记录
    invalid_count = schedule_data['plan_departure_time'].isna().sum()
    if invalid_count > 0:
        logger.warning("发现 %d 条记录的 plan_departure_time 无效，将被删除", invalid_count)
        logger.info("无效 plan_departure_time 样例: %s", schedule_data[schedule_data['plan_departure_time'].isna()][['assign_name', 'dispatch_departure_time', 'departure_time_str']].head().to_dict())
    schedule_data.dropna(subset=['plan_departure_time'], inplace=True)

    # 映射 route_name 到通道
    invalid_routes = []
    def map_to_channel(route_name):
        if pd.isna(route_name) or route_name is None:
            invalid_routes.append('None')
            return None
        route_name = route_name.strip().replace('  ', ' ')
        for channel, routes in CHANNEL_MAPPING.items():
            if route_name in [r.strip().replace('  ', ' ') for r in routes]:
                return channel
        invalid_routes.append(route_name)
        return None

    schedule_data['channel'] = schedule_data['route_name'].apply(map_to_channel)

    # 删除无法映射的记录
    invalid_channel_count = schedule_data['channel'].isna().sum()
    if invalid_channel_count > 0:
        logger.warning("发现 %d 条记录的 route_name 无法映射，将被删除", invalid_channel_count)
        logger.info("无法映射的 route_name 样例: %s", list(set(invalid_routes))[:10])
    schedule_data.dropna(subset=['channel'], inplace=True)

    # 记录通道分配统计
    channel_counts = schedule_data['channel'].value_counts().to_dict()
    logger.info("通道分配统计: %s", channel_counts)

    logger.info("调度数据日期拼接完成，样例: %s", schedule_data[['plan_departure_time', 'plan_end_time', 'single_trip_duration', 'terminal_id', 'route_name', 'channel']].head().to_dict())
    logger.info("过滤后调度数据记录数: %d", len(schedule_data))
    return schedule_data

# 数据清洗与特征工程
def preprocess_data(trade_data, broadcast_data, schedule_data, weather_data):
    logger.info("开始数据清洗与特征工程...")

    # 时间格式统一
    trade_data['trade_time'] = pd.to_datetime(trade_data['trade_time'])
    broadcast_data['arrive_time'] = pd.to_datetime(broadcast_data['arrive_time'])
    broadcast_data['leave_time'] = pd.to_datetime(broadcast_data['leave_time'])
    weather_data['datetime'] = pd.to_datetime(weather_data['datetime'])

    # 处理调度数据日期并映射通道
    if not schedule_data.empty:
        schedule_data = process_schedule_dates(schedule_data)
    else:
        logger.warning("调度数据为空，生成默认调度数据")

    # 按15分钟时间段对齐
    trade_data['time_slot'] = trade_data['trade_time'].dt.floor('15min')
    broadcast_data['time_slot'] = broadcast_data['arrive_time'].dt.floor('15min')
    if not schedule_data.empty:
        schedule_data['time_slot'] = schedule_data['plan_departure_time'].dt.floor('15min')

    # 映射交易和报站数据到通道
    invalid_routes = []
    def map_to_channel(route_name):
        if pd.isna(route_name) or route_name is None:
            invalid_routes.append('None')
            return None
        route_name = route_name.strip().replace('  ', ' ')
        for channel, routes in CHANNEL_MAPPING.items():
            if route_name in [r.strip().replace('  ', ' ') for r in routes]:
                return channel
        invalid_routes.append(route_name)
        return None

    trade_data['channel'] = trade_data['route_name'].apply(map_to_channel)
    broadcast_data['channel'] = broadcast_data['route_name'].apply(map_to_channel)

    # 删除无法映射的记录
    trade_invalid_count = trade_data['channel'].isna().sum()
    if trade_invalid_count > 0:
        logger.warning("交易数据中发现 %d 条记录的 route_name 无法映射，将被删除", trade_invalid_count)
        logger.info("交易数据无法映射的 route_name 样例: %s", list(set(invalid_routes))[:10])
    trade_data.dropna(subset=['channel'], inplace=True)

    broadcast_invalid_count = broadcast_data['channel'].isna().sum()
    if broadcast_invalid_count > 0:
        logger.warning("报站数据中发现 %d 条记录的 route_name 无法映射，将被删除", broadcast_invalid_count)
        logger.info("报站数据无法映射的 route_name 样例: %s", list(set(invalid_routes))[:10])
    broadcast_data.dropna(subset=['channel'], inplace=True)

    # 计算总客流
    trade_flow = trade_data.groupby(['time_slot', 'channel']).size().reset_index(name='total_flow')
    broadcast_flow = broadcast_data.groupby(['time_slot', 'channel']).agg({
        'board_amount': 'sum',
        'off_amount': 'sum'
    }).reset_index()
    broadcast_flow['total_flow'] = broadcast_flow['board_amount'] + broadcast_flow['off_amount']

    flow_data = trade_flow.merge(
        broadcast_flow[['time_slot', 'channel', 'total_flow']],
        on=['time_slot', 'channel'],
        how='left',
        suffixes=('_trade', '_broadcast')
    )
    flow_data['total_flow'] = flow_data['total_flow_trade'].fillna(flow_data['total_flow_broadcast'])
    flow_data.drop(['total_flow_trade', 'total_flow_broadcast'], axis=1, inplace=True)
    logger.info("合并客流数据记录数: %d", len(flow_data))

    # 异常值处理
    flow_mean = flow_data['total_flow'].mean()
    flow_std = flow_data['total_flow'].std()
    flow_data['total_flow'] = np.clip(flow_data['total_flow'], flow_mean - 3 * flow_std, flow_mean + 3 * flow_std)
    logger.info("客流异常值处理完成，均值: %.2f, 标准差: %.2f", flow_mean, flow_std)

    # 调度特征
    if schedule_data.empty or schedule_data['plan_departure_time'].isna().all():
        logger.warning("调度数据为空或全为NaN，生成默认调度数据")
        time_slots = flow_data['time_slot'].unique()
        channels = list(CHANNEL_MAPPING.keys())
        schedule_data = pd.DataFrame([
            {'time_slot': ts, 'channel': ch, 'class_interval': 30, 'class_frequency': 1, 'single_trip_duration': 10}
            for ts in time_slots for ch in channels
        ])
    else:
        schedule_data['class_interval'] = schedule_data.groupby(['time_slot', 'channel'])['plan_departure_time'].diff().dt.total_seconds() / 60
        schedule_data['class_interval'] = schedule_data['class_interval'].fillna(schedule_data['class_interval'].median())
        if schedule_data['class_interval'].isna().all():
            logger.warning("班次间隔全为NaN，使用默认值30分钟")
            schedule_data['class_interval'] = 30
        schedule_data['class_interval'] = np.clip(schedule_data['class_interval'], 5, 60)
        schedule_data['class_frequency'] = schedule_data.groupby(['time_slot', 'channel']).size().reindex(schedule_data[['time_slot', 'channel']]).fillna(1).values
        schedule_data['single_trip_duration'] = schedule_data['single_trip_duration'].fillna(10)
    logger.info("调度数据样例: %s", schedule_data.head().to_dict())
    logger.info("调度特征处理完成，班次间隔中位数: %.2f", schedule_data['class_interval'].median() if not schedule_data['class_interval'].isna().all() else 30)

    # 时间特征
    flow_data['hour'] = flow_data['time_slot'].dt.hour
    flow_data['minute'] = flow_data['time_slot'].dt.minute
    flow_data['weekday'] = flow_data['time_slot'].dt.weekday
    flow_data['is_holiday'] = flow_data['time_slot'].dt.date.apply(lambda x: 1 if x in China(years=2025) else 0)
    logger.info("时间特征提取完成，节假日记录数: %d", flow_data['is_holiday'].sum())

    # 天气特征
    weather_data['time_slot'] = weather_data['datetime'].dt.floor('15min')
    scaler = MinMaxScaler()
    weather_data[['temperature', 'precipitation', 'wind_speed']] = scaler.fit_transform(
        weather_data[['temperature', 'precipitation', 'wind_speed']]
    )
    enc = OneHotEncoder(sparse_output=False)
    weather_type_encoded = enc.fit_transform(weather_data[['weather_type']])
    weather_type_df = pd.DataFrame(weather_type_encoded, columns=enc.get_feature_names_out(['weather_type']))
    weather_data = pd.concat([weather_data, weather_type_df], axis=1)
    logger.info("天气特征归一化与编码完成")

    # 扩展天气数据时间范围
    time_slots = flow_data['time_slot'].unique()
    weather_template = pd.DataFrame({'time_slot': time_slots})
    weather_data = weather_template.merge(weather_data, on='time_slot', how='left')
    weather_data[['temperature', 'precipitation', 'wind_speed']] = weather_data[['temperature', 'precipitation', 'wind_speed']].fillna(weather_data[['temperature', 'precipitation', 'wind_speed']].mean())
    for col in enc.get_feature_names_out(['weather_type']):
        weather_data[col] = weather_data[col].fillna(0)
        weather_data.loc[weather_data[col].isna(), col] = 1 if 'sunny' in col else 0

    # 合并数据集
    data = flow_data.merge(
        schedule_data[['time_slot', 'channel', 'class_interval', 'class_frequency', 'single_trip_duration']],
        on=['time_slot', 'channel'],
        how='left'
    )
    data = data.merge(
        weather_data[['time_slot', 'temperature', 'precipitation', 'wind_speed'] + list(enc.get_feature_names_out(['weather_type']))],
        on='time_slot',
        how='left'
    )

    # 检查并填充NaN
    logger.info("检查合并后数据的NaN值...")
    for column in data.columns:
        nan_count = data[column].isna().sum()
        if nan_count > 0:
            logger.warning("特征 %s 包含 %d 个NaN值", column, nan_count)
            if column in ['class_interval', 'class_frequency', 'single_trip_duration']:
                default_value = 30 if column == 'class_interval' else 1 if column == 'class_frequency' else 10
                data[column].fillna(default_value, inplace=True)
            elif column in ['temperature', 'precipitation', 'wind_speed'] + list(enc.get_feature_names_out(['weather_type'])):
                data[column].fillna(data[column].mean(), inplace=True)
            else:
                data[column].fillna(0, inplace=True)
    logger.info("合并后数据样例: %s", data.head().to_dict())
    logger.info("数据集合并完成，记录数: %d", len(data))

    return data, scaler, enc

# 通道权重计算与客流预测
def train_gpr_and_predict(data, scaler, enc):
    logger.info("开始训练GPR模型并预测通道客流...")
    channels = ['C1', 'C2', 'C3', 'C4', 'C5']
    initial_weights = np.array([0.2] * 5)
    logger.info("初始通道权重: %s", initial_weights)

    # 特征矩阵
    features = ['hour', 'minute', 'weekday', 'is_holiday', 'class_interval', 'class_frequency', 'single_trip_duration',
                'temperature', 'precipitation', 'wind_speed'] + list(enc.get_feature_names_out(['weather_type']))

    # 为每个通道训练 GPR 模型
    channel_flows = []
    gpr_models = {}
    for channel in channels:
        channel_data = data[data['channel'] == channel].copy()
        if channel_data.empty:
            logger.warning("通道 %s 数据为空，使用默认特征", channel)
            channel_data = data.iloc[-1:].copy()
            channel_data['class_interval'] = 30
            channel_data['class_frequency'] = 1
            channel_data['single_trip_duration'] = 10

        X_channel = channel_data[features].values
        y_channel = channel_data['total_flow'].values

        if len(y_channel) == 0:
            logger.warning("通道 %s 无有效客流数据，使用默认预测值", channel)
            channel_flows.append(10.0)  # 默认预测均值
            continue

        # 检查特征矩阵X中的NaN
        if np.any(np.isnan(X_channel)):
            nan_columns = [col for col, has_nan in zip(features, np.any(np.isnan(X_channel), axis=0)) if has_nan]
            logger.warning("通道 %s 特征矩阵包含NaN，涉及特征: %s", channel, nan_columns)
            for i, col in enumerate(features):
                if np.any(np.isnan(X_channel[:, i])):
                    X_channel[:, i] = np.nan_to_num(X_channel[:, i], nan=data[col].median() if not data[col].isna().all() else 0)

        # 训练 GPR 模型
        kernel = RBF(length_scale=1.0) + WhiteKernel(noise_level=1.0)
        gpr = GaussianProcessRegressor(kernel=kernel, random_state=42)
        gpr.fit(X_channel, y_channel)
        gpr_models[channel] = gpr
        y_pred, _ = gpr.predict(X_channel, return_std=True)
        channel_flows.append(y_pred.mean())
        logger.info("通道 %s 预测客流均值: %.2f", channel, y_pred.mean())

    # 计算通道权重
    channel_flows = np.array(channel_flows)
    weights = channel_flows / np.sum(channel_flows)
    weights = np.clip(weights, 0.1, 0.4)
    weights = weights / np.sum(weights)  # 归一化
    logger.info("更新后的通道权重: %s", weights)

    # 预测未来15、30、60分钟客流
    now = datetime.now()
    future_times = [now + timedelta(minutes=x) for x in [15, 30, 60]]
    predictions = {}
    for t in future_times:
        future_slot = pd.Timestamp(t).replace(second=0, microsecond=0).floor('15min')
        channel_predictions = {}
        for channel in channels:
            future_data = data[(data['time_slot'] == future_slot) & (data['channel'] == channel)].copy()
            if future_data.empty:
                logger.warning("通道 %s 未来时间 %s 无历史数据，使用最近数据填充", channel, future_slot)
                future_data = data[data['channel'] == channel].iloc[-1:].copy()
                if future_data.empty:
                    future_data = data.iloc[-1:].copy()
                    future_data['channel'] = channel
                future_data['time_slot'] = future_slot
                future_data['hour'] = future_slot.hour
                future_data['minute'] = future_slot.minute
                future_data['weekday'] = future_slot.weekday()
                future_data['is_holiday'] = 1 if future_slot.date() in China(years=2025) else 0
                future_data['class_interval'] = future_data['class_interval'].fillna(30)
                future_data['class_frequency'] = future_data['class_frequency'].fillna(1)
                future_data['single_trip_duration'] = future_data['single_trip_duration'].fillna(10)
                future_data['temperature'] = future_data['temperature'].fillna(data['temperature'].mean())
                future_data['precipitation'] = future_data['precipitation'].fillna(data['precipitation'].mean())
                future_data['wind_speed'] = future_data['wind_speed'].fillna(data['wind_speed'].mean())
                for col in enc.get_feature_names_out(['weather_type']):
                    future_data[col] = future_data[col].fillna(1 if 'sunny' in col else 0)

            X_future = future_data[features].values
            if np.any(np.isnan(X_future)):
                logger.warning("通道 %s 未来时间 %s 的特征矩阵包含NaN，尝试填充", channel, future_slot)
                for i, col in enumerate(features):
                    if np.any(np.isnan(X_future[:, i])):
                        X_future[:, i] = np.nan_to_num(X_future[:, i], nan=data[col].median() if not data[col].isna().all() else 0)

            gpr = gpr_models.get(channel, GaussianProcessRegressor(kernel=RBF(length_scale=1.0) + WhiteKernel(noise_level=1.0), random_state=42))
            total_flow_pred, _ = gpr.predict(X_future, return_std=True)
            channel_predictions[f'Channel {channels.index(channel) + 1}'] = total_flow_pred[0] * weights[channels.index(channel)]

        predictions[t] = channel_predictions
        logger.info("未来时间 %s 通道客流预测: %s", t, predictions[t])

    return predictions, weights

def main():
    logger.info("启动灵隐寺站点客流预测程序...")
    trade_data, broadcast_data, schedule_data = fetch_data_from_db()
    weather_data = fetch_real_time_weather('2025-06-01', '2025-06-30')
    data, scaler, enc = preprocess_data(trade_data, broadcast_data, schedule_data, weather_data)
    predictions, weights = train_gpr_and_predict(data, scaler, enc)
    for time, pred in predictions.items():
        logger.info("预测时间: %s", time)
        for channel, flow in pred.items():
            logger.info("%s 客流预测: %.2f", channel, flow)

if __name__ == "__main__":
    main()