import logging
import psycopg2
import psycopg2.extras
import pandas as pd
import numpy as np
from datetime import datetime, timedelta
from holidays import China
from sklearn.preprocessing import MinMaxScaler, OneHotEncoder
from sklearn.gaussian_process import GaussianProcessRegressor
from sklearn.gaussian_process.kernels import RBF, WhiteKernel
from sklearn.metrics import mean_squared_error, mean_absolute_error, r2_score
import warnings
import requests
from flask import Flask, jsonify

warnings.filterwarnings("ignore")

# 配置
CONFIG = {
    'time_granularity': '15min',  # 数据聚合时间粒度，可选 '15min', '30min', '1h' 等
    'window_days': 30,  # 滑动窗口天数
    'db_config': {
        'host': '20.17.39.23',
        'port': 5432,
        'user': 'gj_dw_r1',
        'password': 'gj_dw_r1',
        'database': 'GJ_DW'
    }
}

# 初始化 Flask 应用
app = Flask(__name__)

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

# 通道与 route_name 映射
CHANNEL_MAPPING = {
    'C1': ['黄龙体育中心至灵隐专线', '武林广场至灵隐专线'],
    'C2': ['278M路', '319M路', '西溪路停车场至灵隐接驳线'],
    'C3': ['505路'],
    'C4': ['7路'],
    'C5': ['龙翔桥至灵隐专线']
}

# 通道名称映射
CHANNEL_NAME_MAPPING = {
    'C1': "'黄龙体育中心至灵隐专线', '武林广场至灵隐专线'",
    'C2': "'278M路', '319M路', '西溪路停车场至灵隐接驳线'",
    'C3': "505路",
    'C4': "7路",
    'C5': "龙翔桥至灵隐专线"
}

# 模拟天气数据
def generate_simulated_weather_data(start_date, end_date, time_granularity):
    logger.info("生成模拟天气数据...")
    dates = pd.date_range(start=start_date, end=end_date, freq=time_granularity)
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
def fetch_real_time_weather(start_date, end_date, time_granularity):
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
        weather_df['time_slot'] = weather_df['datetime'].dt.floor(time_granularity)
        weather_df = weather_df.groupby('time_slot').agg({
            'temperature': 'mean',
            'precipitation': 'sum',
            'wind_speed': 'mean',
            'weather_type': lambda x: x.mode()[0] if not x.empty else 'cloudy'
        }).reset_index()
        weather_df.rename(columns={'time_slot': 'datetime'}, inplace=True)
        logger.info("实时天气数据获取完成，记录数: %d", len(weather_df))
        return weather_df
    except Exception as e:
        logger.error("获取天气数据失败: %s", str(e))
        logger.warning("使用模拟数据作为后备")
        return generate_simulated_weather_data(start_date, end_date, time_granularity)

def map_weather_code(code):
    if code in [0, 1]: return 'sunny'
    elif code in [2, 3]: return 'cloudy'
    elif code in [51, 53, 55, 61, 63, 65, 80, 81, 82]: return 'rainy'
    return 'cloudy'

# 数据提取
def fetch_data_from_db(window_days):
    logger.info("开始从数据库提取数据...")
    try:
        conn = psycopg2.connect(**CONFIG['db_config'])
        cursor = conn.cursor(cursor_factory=psycopg2.extras.DictCursor)

        end_date = datetime.now()
        start_date = end_date - timedelta(days=window_days)
        start_date_str = start_date.strftime('%Y-%m-%d %H:%M:%S')
        end_date_str = end_date.strftime('%Y-%m-%d %H:%M:%S')

        query_trade = """
        SELECT trade_time, stop_id, off_stop_id, route_id, direction, route_name
        FROM ods.t_dm_trade_zhipu
        WHERE (stop_id = '1001001154' OR off_stop_id = '1001001154')
        AND trade_time >= %s AND trade_time < %s
        """
        cursor.execute(query_trade, (start_date_str, end_date_str))
        trade_data = pd.DataFrame([dict(row) for row in cursor.fetchall()])
        logger.info("提取交易数据记录数: %d", len(trade_data))

        query_broadcast = """
        SELECT arrive_time, leave_time, board_amount, off_amount, on_bus_amount, stop_id, route_name
        FROM ods.t_dm_sim_station_broacast_zhipu
        WHERE stop_id = '1001001154'
        AND arrive_time >= %s AND arrive_time < %s
        """
        cursor.execute(query_broadcast, (start_date_str, end_date_str))
        broadcast_data = pd.DataFrame([dict(row) for row in cursor.fetchall()])
        logger.info("提取报站数据记录数: %d", len(broadcast_data))

        query_schedule = """
        SELECT assign_name, dispatch_departure_time, dispatch_end_time, single_trip_duration, terminal_id, route_name
        FROM ods.dispatch_order_departure
        WHERE (origin_id = '1001001154' OR terminal_id = '1001001154')
        AND dispatch_departure_time >= %s AND dispatch_departure_time < %s
        """
        cursor.execute(query_schedule, (start_date_str, end_date_str))
        schedule_data = pd.DataFrame([dict(row) for row in cursor.fetchall()])
        logger.info("提取调度数据记录数: %d", len(schedule_data))
        logger.info("调度数据原始样例: %s", schedule_data[['assign_name', 'dispatch_departure_time', 'dispatch_end_time', 'route_name']].head().to_dict())

        cursor.close()
        conn.close()
        return trade_data, broadcast_data, schedule_data
    except Exception as e:
        logger.error("数据库查询失败: %s", str(e))
        raise

# 调度数据处理与通道映射
def process_schedule_dates(schedule_data):
    logger.info("开始处理调度数据...")
    schedule_data['plan_departure_time'] = pd.to_datetime(schedule_data['dispatch_departure_time'], errors='coerce')
    schedule_data['plan_end_time'] = pd.to_datetime(schedule_data['dispatch_end_time'], errors='coerce')

    start_date = (datetime.now() - timedelta(days=CONFIG['window_days'])).replace(hour=0, minute=0, second=0, microsecond=0)
    end_date = datetime.now().replace(hour=23, minute=59, second=59, microsecond=999999)
    schedule_data = schedule_data[
        (schedule_data['plan_departure_time'] >= start_date) &
        (schedule_data['plan_departure_time'] < end_date)
        ]

    schedule_data['single_trip_duration'] = np.clip(schedule_data['single_trip_duration'], 5, 60)
    schedule_data['single_trip_duration'] = schedule_data['single_trip_duration'].fillna(10)

    invalid_count = schedule_data['plan_departure_time'].isna().sum()
    if invalid_count > 0:
        logger.warning("发现 %d 条记录的 plan_departure_time 无效，将被删除", invalid_count)
        logger.info("无效 plan_departure_time 样例: %s", schedule_data[schedule_data['plan_departure_time'].isna()][['assign_name', 'dispatch_departure_time']].head().to_dict())
    schedule_data.dropna(subset=['plan_departure_time'], inplace=True)

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

    invalid_channel_count = schedule_data['channel'].isna().sum()
    if invalid_channel_count > 0:
        logger.warning("发现 %d 条记录的 route_name 无法映射，将被删除", invalid_channel_count)
        logger.info("无法映射的 route_name 样例: %s", list(set(invalid_routes))[:10])
    schedule_data.dropna(subset=['channel'], inplace=True)

    channel_counts = schedule_data['channel'].value_counts().to_dict()
    logger.info("通道分配统计: %s", channel_counts)

    logger.info("调度数据处理完成，样例: %s", schedule_data[['plan_departure_time', 'plan_end_time', 'single_trip_duration', 'terminal_id', 'route_name', 'channel']].head().to_dict())
    logger.info("过滤后调度数据记录数: %d", len(schedule_data))
    return schedule_data

# 数据清洗与特征工程
def preprocess_data(trade_data, broadcast_data, schedule_data, weather_data, time_granularity):
    logger.info("开始数据清洗与特征工程，时间粒度: %s", time_granularity)
    trade_data['trade_time'] = pd.to_datetime(trade_data['trade_time'])
    broadcast_data['arrive_time'] = pd.to_datetime(broadcast_data['arrive_time'])
    broadcast_data['leave_time'] = pd.to_datetime(broadcast_data['leave_time'])
    weather_data['datetime'] = pd.to_datetime(weather_data['datetime'])

    if not schedule_data.empty:
        schedule_data = process_schedule_dates(schedule_data)
    else:
        logger.warning("调度数据为空，生成默认调度数据")

    trade_data['time_slot'] = trade_data['trade_time'].dt.floor(time_granularity)
    broadcast_data['time_slot'] = broadcast_data['arrive_time'].dt.floor(time_granularity)
    if not schedule_data.empty:
        schedule_data['time_slot'] = schedule_data['plan_departure_time'].dt.floor(time_granularity)

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

    flow_mean = flow_data['total_flow'].mean()
    flow_std = flow_data['total_flow'].std()
    flow_data['total_flow'] = np.clip(flow_data['total_flow'], flow_mean - 3 * flow_std, flow_mean + 3 * flow_std)
    logger.info("客流异常值处理完成，均值: %.2f, 标准差: %.2f", flow_mean, flow_std)

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

    flow_data['hour'] = flow_data['time_slot'].dt.hour
    flow_data['minute'] = flow_data['time_slot'].dt.minute
    flow_data['weekday'] = flow_data['time_slot'].dt.weekday
    flow_data['is_holiday'] = flow_data['time_slot'].dt.date.apply(lambda x: 1 if x in China(years=datetime.now().year) else 0)
    logger.info("时间特征提取完成，节假日记录数: %d", flow_data['is_holiday'].sum())

    weather_data['time_slot'] = weather_data['datetime'].dt.floor(time_granularity)
    scaler = MinMaxScaler()
    weather_data[['temperature', 'precipitation', 'wind_speed']] = scaler.fit_transform(
        weather_data[['temperature', 'precipitation', 'wind_speed']]
    )
    enc = OneHotEncoder(sparse_output=False)
    weather_type_encoded = enc.fit_transform(weather_data[['weather_type']])
    weather_type_df = pd.DataFrame(weather_type_encoded, columns=enc.get_feature_names_out(['weather_type']))
    weather_data = pd.concat([weather_data, weather_type_df], axis=1)
    logger.info("天气特征归一化与编码完成")

    time_slots = flow_data['time_slot'].unique()
    weather_template = pd.DataFrame({'time_slot': time_slots})
    weather_data = weather_template.merge(weather_data, on='time_slot', how='left')
    weather_data[['temperature', 'precipitation', 'wind_speed']] = weather_data[['temperature', 'precipitation', 'wind_speed']].fillna(weather_data[['temperature', 'precipitation', 'wind_speed']].mean())
    for col in enc.get_feature_names_out(['weather_type']):
        weather_data[col] = weather_data[col].fillna(0)
        weather_data.loc[weather_data[col].isna(), col] = 1 if 'sunny' in col else 0

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

# 计算 MAPE
def mean_absolute_percentage_error(y_true, y_pred):
    y_true, y_pred = np.array(y_true), np.array(y_pred)
    non_zero = y_true != 0
    if np.sum(non_zero) == 0:
        logger.warning("MAPE 计算中所有真实值为0，返回0")
        return 0.0
    return np.mean(np.abs((y_true[non_zero] - y_pred[non_zero]) / y_true[non_zero])) * 100

# 通道权重计算与客流预测（含验证逻辑）
def train_gpr_and_predict(data, scaler, enc, window_days, time_granularity, validate=False):
    logger.info("开始训练GPR模型并预测通道客流，滑动窗口天数: %d, 验证模式: %s", window_days, validate)
    window_start = data['time_slot'].max() - timedelta(days=window_days)

    # 划分训练集和验证集
    if validate:
        train_end = data['time_slot'].max() - timedelta(days=1)  # 保留最后一天作为验证集
        train_data = data[(data['time_slot'] >= window_start) & (data['time_slot'] < train_end)].copy()
        valid_data = data[data['time_slot'] >= train_end].copy()
        logger.info("训练数据记录数: %d, 验证数据记录数: %d", len(train_data), len(valid_data))
    else:
        train_data = data[data['time_slot'] >= window_start].copy()
        valid_data = pd.DataFrame()  # 空验证集
        logger.info("训练数据记录数: %d", len(train_data))

    channels = ['C1', 'C2', 'C3', 'C4', 'C5']
    initial_weights = np.array([0.2] * 5)
    logger.info("初始通道权重: %s", initial_weights)

    features = ['hour', 'minute', 'weekday', 'is_holiday', 'class_interval', 'class_frequency', 'single_trip_duration',
                'temperature', 'precipitation', 'wind_speed', 'time_horizon'] + list(enc.get_feature_names_out(['weather_type']))

    train_data['time_horizon'] = 0
    if validate:
        valid_data['time_horizon'] = 0

    channel_flows = []
    gpr_models = {}
    valid_results = []

    # 训练和验证
    for channel in channels:
        channel_data = train_data[train_data['channel'] == channel].copy()
        if channel_data.empty:
            logger.warning("通道 %s 训练数据为空，使用默认特征", channel)
            channel_data = train_data.iloc[-1:].copy()
            channel_data['class_interval'] = 30
            channel_data['class_frequency'] = 1
            channel_data['single_trip_duration'] = 10
            channel_data['time_horizon'] = 0

        X_channel = channel_data[features].values
        y_channel = channel_data['total_flow'].values

        if len(y_channel) < 10:
            logger.warning("通道 %s 训练数据不足（记录数: %d），使用默认预测值", channel, len(y_channel))
            channel_flows.append(10.0)
            continue

        if np.any(np.isnan(X_channel)):
            nan_columns = [col for col, has_nan in zip(features, np.any(np.isnan(X_channel), axis=0)) if has_nan]
            logger.warning("通道 %s 特征矩阵包含NaN，涉及特征: %s", channel, nan_columns)
            for i, col in enumerate(features):
                if np.any(np.isnan(X_channel[:, i])):
                    default_value = train_data[col].median() if not train_data[col].isna().all() else 0
                    X_channel[:, i] = np.nan_to_num(X_channel[:, i], nan=default_value)

        try:
            kernel = RBF(length_scale=1.0) + WhiteKernel(noise_level=1.0)
            gpr = GaussianProcessRegressor(kernel=kernel, random_state=42)
            gpr.fit(X_channel, y_channel)
            gpr_models[channel] = gpr
            y_pred, _ = gpr.predict(X_channel, return_std=True)
            channel_flows.append(max(y_pred.mean(), 1.0))
            logger.info("通道 %s 预测客流均值: %.2f", channel, y_pred.mean())
        except Exception as e:
            logger.error("通道 %s GPR模型训练失败: %s", channel, str(e))
            channel_flows.append(10.0)
            continue

        # 验证集预测
        if validate and not valid_data.empty:
            valid_channel_data = valid_data[valid_data['channel'] == channel].copy()
            if valid_channel_data.empty:
                logger.warning("通道 %s 验证数据为空，跳过验证", channel)
                continue
            X_valid = valid_channel_data[features].values
            y_valid = valid_channel_data['total_flow'].values
            if np.any(np.isnan(X_valid)):
                logger.warning("通道 %s 验证集特征矩阵包含NaN，尝试填充", channel)
                for i, col in enumerate(features):
                    if np.any(np.isnan(X_valid[:, i])):
                        default_value = train_data[col].median() if not train_data[col].isna().all() else 0
                        X_valid[:, i] = np.nan_to_num(X_valid[:, i], nan=default_value)
            try:
                y_pred_valid, _ = gpr.predict(X_valid, return_std=True)
                valid_channel_data['predicted_flow'] = y_pred_valid
                valid_channel_data['actual_flow'] = y_valid
                valid_results.append(valid_channel_data[['time_slot', 'channel', 'actual_flow', 'predicted_flow']])
                logger.info("通道 %s 验证集预测完成，记录数: %d", channel, len(valid_channel_data))
            except Exception as e:
                logger.error("通道 %s 验证集预测失败: %s", channel, str(e))
                continue

    channel_flows = np.array(channel_flows)
    if np.all(channel_flows == 0):
        logger.warning("所有通道预测客流为0，使用初始权重")
        weights = initial_weights
    else:
        weights = channel_flows / np.sum(channel_flows)
        weights = np.clip(weights, 0.1, 0.4)
        weights = weights / np.sum(weights)
    logger.info("更新后的通道权重: %s", weights)

    # 计算验证指标
    metrics = {}
    if validate and valid_results:
        valid_results_df = pd.concat(valid_results, ignore_index=True)
        if not valid_results_df.empty:
            metrics['overall'] = {
                'MSE': mean_squared_error(valid_results_df['actual_flow'], valid_results_df['predicted_flow']),
                'MAE': mean_absolute_error(valid_results_df['actual_flow'], valid_results_df['predicted_flow']),
                'MAPE': mean_absolute_percentage_error(valid_results_df['actual_flow'], valid_results_df['predicted_flow']),
                'R2': r2_score(valid_results_df['actual_flow'], valid_results_df['predicted_flow'])
            }
            logger.info("整体验证指标: MSE=%.2f, MAE=%.2f, MAPE=%.2f%%, R2=%.2f",
                        metrics['overall']['MSE'], metrics['overall']['MAE'], metrics['overall']['MAPE'], metrics['overall']['R2'])

            # 按通道计算指标
            metrics['by_channel'] = {}
            for channel in channels:
                channel_results = valid_results_df[valid_results_df['channel'] == channel]
                if not channel_results.empty:
                    metrics['by_channel'][channel] = {
                        'MSE': mean_squared_error(channel_results['actual_flow'], channel_results['predicted_flow']),
                        'MAE': mean_absolute_error(channel_results['actual_flow'], channel_results['predicted_flow']),
                        'MAPE': mean_absolute_percentage_error(channel_results['actual_flow'], channel_results['predicted_flow']),
                        'R2': r2_score(channel_results['actual_flow'], channel_results['predicted_flow'])
                    }
                    logger.info("通道 %s 验证指标: MSE=%.2f, MAE=%.2f, MAPE=%.2f%%, R2=%.2f",
                                channel, metrics['by_channel'][channel]['MSE'], metrics['by_channel'][channel]['MAE'],
                                metrics['by_channel'][channel]['MAPE'], metrics['by_channel'][channel]['R2'])
    else:
        valid_results_df = pd.DataFrame()

    # 预测未来客流
    now = datetime.now()
    granularity_minutes = {'15min': 15, '30min': 30, '1h': 60}.get(time_granularity, 15)
    future_times = [now + timedelta(minutes=x) for x in [granularity_minutes, granularity_minutes*2, granularity_minutes*4]]
    horizon_values = [granularity_minutes, granularity_minutes*2, granularity_minutes*4]
    predictions = []
    for idx, (t, horizon) in enumerate(zip(future_times, horizon_values)):
        future_slot = pd.Timestamp(t).replace(second=0, microsecond=0).floor(time_granularity)
        for channel_idx, channel in enumerate(channels, 1):
            future_data = train_data[(train_data['time_slot'] == future_slot) & (train_data['channel'] == channel)].copy()
            if future_data.empty:
                logger.warning("通道 %s 未来时间 %s 无历史数据，使用最近数据填充", channel, future_slot)
                recent_data = train_data[train_data['channel'] == channel].copy()
                if not recent_data.empty:
                    recent_data = recent_data.iloc[-1:]
                else:
                    recent_data = train_data.iloc[-1:].copy()
                    recent_data['channel'] = channel
                future_data = recent_data.copy()
                future_data['time_slot'] = future_slot
                future_data['hour'] = future_slot.hour
                future_data['minute'] = future_slot.minute
                future_data['weekday'] = future_slot.weekday()
                future_data['is_holiday'] = 1 if future_slot.date() in China(years=datetime.now().year) else 0
                future_data['class_interval'] = future_data['class_interval'].fillna(30)
                future_data['class_frequency'] = future_data['class_frequency'].fillna(1)
                future_data['single_trip_duration'] = future_data['single_trip_duration'].fillna(10)
                future_data['temperature'] = future_data['temperature'].fillna(train_data['temperature'].mean())
                future_data['precipitation'] = future_data['precipitation'].fillna(train_data['precipitation'].mean())
                future_data['wind_speed'] = future_data['wind_speed'].fillna(train_data['wind_speed'].mean())
                future_data['time_horizon'] = horizon / 60.0
                for col in enc.get_feature_names_out(['weather_type']):
                    future_data[col] = future_data[col].fillna(1 if 'sunny' in col else 0)

            X_future = future_data[features].values
            if np.any(np.isnan(X_future)):
                logger.warning("通道 %s 未来时间 %s 的特征矩阵包含NaN，尝试填充", channel, future_slot)
                for i, col in enumerate(features):
                    if np.any(np.isnan(X_future[:, i])):
                        default_value = train_data[col].median() if not train_data[col].isna().all() else 0
                        X_future[:, i] = np.nan_to_num(X_future[:, i], nan=default_value)

            try:
                gpr = gpr_models.get(channel, GaussianProcessRegressor(kernel=RBF(length_scale=1.0) + WhiteKernel(noise_level=1.0), random_state=42))
                total_flow_pred, _ = gpr.predict(X_future, return_std=True)
                flow_pred = int(max(total_flow_pred[0] * weights[channels.index(channel)] * (horizon / granularity_minutes), 1))
            except Exception as e:
                logger.error("通道 %s 未来时间 %s 预测失败: %s", channel, future_slot, str(e))
                flow_pred = 10

            prediction = {
                'passengewayIndex': channel_idx,
                'passengewayName': CHANNEL_NAME_MAPPING[channel],
                'instationMin15': flow_pred if idx == 0 else 0,
                'instationMin30': flow_pred if idx == 1 else 0,
                'instationMin60': flow_pred if idx == 2 else 0
            }
            predictions.append(prediction)

    result = []
    for channel_idx in range(1, 6):
        channel_preds = [p for p in predictions if p['passengewayIndex'] == channel_idx]
        try:
            combined_pred = {
                'passengewayIndex': channel_idx,
                'passengewayName': channel_preds[0]['passengewayName'],
                'instationMin15': next(p['instationMin15'] for p in channel_preds if p['instationMin15'] >= 1),
                'instationMin30': next(p['instationMin30'] for p in channel_preds if p['instationMin30'] >= 1),
                'instationMin60': next(p['instationMin60'] for p in channel_preds if p['instationMin60'] >= 1)
            }
            result.append(combined_pred)
        except StopIteration:
            logger.error("通道 %d 合并预测结果失败，使用默认值", channel_idx)
            result.append({
                'passengewayIndex': channel_idx,
                'passengewayName': CHANNEL_NAME_MAPPING[channels[channel_idx-1]],
                'instationMin15': 10,
                'instationMin30': 10,
                'instationMin60': 10
            })

    logger.info("通道客流预测结果: %s", result)
    return result, valid_results_df, metrics

# Flask 接口 - 预测
@app.route('/station/passenger/forecast/passengewayList', methods=['GET'])
def get_passenger_forecast():
    try:
        logger.info("接收到客流预测请求")
        trade_data, broadcast_data, schedule_data = fetch_data_from_db(CONFIG['window_days'])
        today = datetime.now().strftime('%Y-%m-%d')
        weather_data = fetch_real_time_weather(today, today, CONFIG['time_granularity'])
        data, scaler, enc = preprocess_data(trade_data, broadcast_data, schedule_data, weather_data, CONFIG['time_granularity'])
        predictions, _, _ = train_gpr_and_predict(data, scaler, enc, CONFIG['window_days'], CONFIG['time_granularity'], validate=False)

        response = {
            'code': '0',
            'data': predictions,
            'message': 'success',
            'status': 200
        }
        return jsonify(response)
    except Exception as e:
        logger.error("预测接口处理失败: %s", str(e), exc_info=True)
        return jsonify({
            'code': '1',
            'data': [],
            'message': f'Error: {str(e)}',
            'status': 500
        })

# Flask 接口 - 验证
@app.route('/station/passenger/forecast/validate', methods=['GET'])
def validate_passenger_forecast():
    try:
        logger.info("接收到客流预测验证请求")
        trade_data, broadcast_data, schedule_data = fetch_data_from_db(CONFIG['window_days'])
        today = datetime.now().strftime('%Y-%m-%d')
        weather_data = fetch_real_time_weather(today, today, CONFIG['time_granularity'])
        data, scaler, enc = preprocess_data(trade_data, broadcast_data, schedule_data, weather_data, CONFIG['time_granularity'])
        predictions, valid_results, metrics = train_gpr_and_predict(data, scaler, enc, CONFIG['window_days'], CONFIG['time_granularity'], validate=True)

        response = {
            'code': '0',
            'data': {
                'predictions': predictions,
                'validation_results': valid_results[['time_slot', 'channel', 'actual_flow', 'predicted_flow']].to_dict(orient='records') if not valid_results.empty else [],
                'metrics': {
                    'overall': metrics.get('overall', {}),
                    'by_channel': metrics.get('by_channel', {})
                }
            },
            'message': '验证成功',
            'status': 200
        }
        return jsonify(response)
    except Exception as e:
        logger.error("验证接口处理失败: %s", str(e), exc_info=True)
        return jsonify({
            'code': '1',
            'data': {
                'predictions': [],
                'validation_results': [],
                'metrics': {}
            },
            'message': f'Error: {str(e)}',
            'status': 500
        })

if __name__ == "__main__":
    app.run(host='0.0.0.0', port=8848)