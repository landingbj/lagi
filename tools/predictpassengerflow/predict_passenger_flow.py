import logging
import psycopg2
import psycopg2.extras
import pandas as pd
import numpy as np
from datetime import datetime, timedelta
from holidays import China
from sklearn.preprocessing import StandardScaler, OneHotEncoder
from xgboost import XGBRegressor
from sklearn.metrics import mean_squared_error, mean_absolute_error, r2_score
from sklearn.model_selection import TimeSeriesSplit
import warnings
import requests
from flask import Flask, jsonify

warnings.filterwarnings("ignore")

# 配置
CONFIG = {
    'time_granularity': '15min',
    'window_days': 30,
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

# 实时天气 API 接口
def fetch_real_time_weather(start_date, end_date, time_granularity):
    logger.info("从 Open-Meteo 获取实时天气数据...")
    url = "https://api.open-meteo.com/v1/forecast"
    params = {
        "latitude": 30.27,
        "longitude": 120.15,
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
    schedule_data.dropna(subset=['channel'], inplace=True)

    channel_counts = schedule_data['channel'].value_counts().to_dict()
    logger.info("通道分配统计: %s", channel_counts)
    return schedule_data

# 数据清洗与特征工程
def preprocess_data(trade_data, broadcast_data, schedule_data, weather_data, time_granularity):
    logger.info("开始数据清洗与特征工程，时间粒度: %s", time_granularity)

    # 去重
    trade_data = trade_data.drop_duplicates(subset=['trade_time', 'stop_id', 'route_name'])
    broadcast_data = broadcast_data.drop_duplicates(subset=['arrive_time', 'stop_id', 'route_name'])
    schedule_data = schedule_data.drop_duplicates(subset=['dispatch_departure_time', 'route_name'])
    logger.info("去重后数据量 - 交易: %d, 报站: %d, 调度: %d", len(trade_data), len(broadcast_data), len(schedule_data))

    # 时间转换
    trade_data['trade_time'] = pd.to_datetime(trade_data['trade_time'])
    broadcast_data['arrive_time'] = pd.to_datetime(broadcast_data['arrive_time'])
    broadcast_data['leave_time'] = pd.to_datetime(broadcast_data['leave_time'])
    weather_data['datetime'] = pd.to_datetime(weather_data['datetime'])

    # 调度数据处理
    if not schedule_data.empty:
        schedule_data = process_schedule_dates(schedule_data)
    else:
        logger.warning("调度数据为空，生成默认调度数据")

    # 时间槽
    trade_data['time_slot'] = trade_data['trade_time'].dt.floor(time_granularity)
    broadcast_data['time_slot'] = broadcast_data['arrive_time'].dt.floor(time_granularity)
    if not schedule_data.empty:
        schedule_data['time_slot'] = schedule_data['plan_departure_time'].dt.floor(time_granularity)

    # 通道映射
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
    trade_data.dropna(subset=['channel'], inplace=True)
    broadcast_data.dropna(subset=['channel'], inplace=True)
    logger.info("无效通道 - 交易: %d, 报站: %d", trade_data['channel'].isna().sum(), broadcast_data['channel'].isna().sum())

    # 客流聚合
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

    # 异常值处理（IQR）
    Q1 = flow_data['total_flow'].quantile(0.25)
    Q3 = flow_data['total_flow'].quantile(0.75)
    IQR = Q3 - Q1
    flow_data = flow_data[(flow_data['total_flow'] >= Q1 - 1.5 * IQR) & (flow_data['total_flow'] <= Q3 + 1.5 * IQR)]
    logger.info("客流异常值处理完成，IQR范围: [%.2f, %.2f]", Q1 - 1.5 * IQR, Q3 + 1.5 * IQR)

    # 检查零值比例
    for channel in ['C1', 'C2', 'C3', 'C4', 'C5']:
        channel_flow = flow_data[flow_data['channel'] == channel]['total_flow']
        zero_ratio = (channel_flow == 0).mean() if not channel_flow.empty else 1.0
        logger.info("通道 %s 零值比例: %.2f%%", channel, zero_ratio * 100)

    # 调度特征
    if schedule_data.empty:
        time_slots = flow_data['time_slot'].unique()
        channels = list(CHANNEL_MAPPING.keys())
        schedule_data = pd.DataFrame([
            {'time_slot': ts, 'channel': ch, 'dispatch_count': 1, 'single_trip_duration': 10}
            for ts in time_slots for ch in channels
        ])
    else:
        dispatch_count = schedule_data.groupby(['time_slot', 'channel']).size().reset_index(name='dispatch_count')
        schedule_data = schedule_data.merge(dispatch_count, on=['time_slot', 'channel'], how='left')
        schedule_data['dispatch_count'] = schedule_data['dispatch_count'].fillna(1)
        schedule_data['single_trip_duration'] = schedule_data['single_trip_duration'].fillna(10)

    # 时间特征
    flow_data['hour'] = flow_data['time_slot'].dt.hour
    flow_data['minute'] = flow_data['time_slot'].dt.minute
    flow_data['hour_sin'] = np.sin(2 * np.pi * flow_data['hour'] / 24)
    flow_data['hour_cos'] = np.cos(2 * np.pi * flow_data['hour'] / 24)
    flow_data['minute_sin'] = np.sin(2 * np.pi * flow_data['minute'] / 60)
    flow_data['minute_cos'] = np.cos(2 * np.pi * flow_data['minute'] / 60)
    flow_data['weekday'] = flow_data['time_slot'].dt.weekday
    flow_data['is_holiday'] = flow_data['time_slot'].dt.date.apply(lambda x: 1 if x in China(years=datetime.now().year) else 0)
    flow_data['is_peak'] = flow_data['hour'].apply(lambda x: 1 if x in [7, 8, 17, 18] else 0)
    logger.info("时间特征提取完成，节假日记录数: %d, 高峰期记录数: %d", flow_data['is_holiday'].sum(), flow_data['is_peak'].sum())

    # 天气特征
    weather_data['time_slot'] = weather_data['datetime'].dt.floor(time_granularity)
    weather_data['temp_comfort'] = weather_data['temperature'].apply(lambda x: 1 if 15 <= x <= 25 else 0)
    weather_data['rain_category'] = pd.cut(
        weather_data['precipitation'],
        bins=[-1, 0, 5, np.inf],
        labels=['no_rain', 'light_rain', 'heavy_rain']
    )
    logger.info("天气数据列: %s", weather_data.columns.tolist())
    logger.info("降雨分类分布: %s", weather_data['rain_category'].value_counts().to_dict())

    enc = OneHotEncoder(sparse_output=False)
    rain_encoded = enc.fit_transform(weather_data[['rain_category']].fillna('no_rain'))
    rain_df = pd.DataFrame(rain_encoded, columns=enc.get_feature_names_out(['rain_category']))
    weather_data = pd.concat([weather_data, rain_df], axis=1)
    weather_data.drop(['rain_category'], axis=1, inplace=True, errors='ignore')
    logger.info("天气特征处理完成，编码后列: %s", weather_data.columns.tolist())

    # 合并数据
    data = flow_data.merge(
        schedule_data[['time_slot', 'channel', 'dispatch_count', 'single_trip_duration']],
        on=['time_slot', 'channel'],
        how='left'
    )
    data = data.merge(
        weather_data[['time_slot', 'temperature', 'precipitation', 'wind_speed', 'temp_comfort'] + list(enc.get_feature_names_out(['rain_category']))],
        on='time_slot',
        how='left'
    )

    # 填充缺失值
    for column in data.columns:
        nan_count = data[column].isna().sum()
        if nan_count > 0:
            logger.warning("特征 %s 包含 %d 个NaN值", column, nan_count)
            if column in ['dispatch_count']:
                data[column].fillna(1, inplace=True)
            elif column in ['single_trip_duration']:
                data[column].fillna(10, inplace=True)
            elif column in ['temperature', 'precipitation', 'wind_speed', 'temp_comfort'] + list(enc.get_feature_names_out(['rain_category'])):
                data[column].fillna(data[column].mean(), inplace=True)
            else:
                data[column].fillna(0, inplace=True)

    # 特征缩放
    features = ['hour_sin', 'hour_cos', 'minute_sin', 'minute_cos', 'weekday', 'is_holiday', 'is_peak',
                'dispatch_count', 'single_trip_duration', 'temperature', 'precipitation', 'wind_speed', 'temp_comfort'] + \
               list(enc.get_feature_names_out(['rain_category']))
    scaler = StandardScaler()
    data[features] = scaler.fit_transform(data[features])
    logger.info("数据集合并完成，记录数: %d", len(data))

    return data, scaler, enc, weather_data

# 计算 MAPE
def mean_absolute_percentage_error(y_true, y_pred):
    y_true, y_pred = np.array(y_true), np.array(y_pred)
    non_zero = y_true != 0
    if np.sum(non_zero) == 0:
        return 0.0
    return np.mean(np.abs((y_true[non_zero] - y_pred[non_zero]) / y_true[non_zero])) * 100

# 训练与预测
def train_gpr_and_predict(data, scaler, enc, weather_data, window_days, time_granularity, validate=False):
    logger.info("开始训练 XGBoost 模型并预测，窗口天数: %d, 验证模式: %s", window_days, validate)
    window_start = data['time_slot'].max() - timedelta(days=window_days)

    # 划分训练和验证集
    if validate:
        train_end = data['time_slot'].max() - timedelta(days=1)
        train_data = data[(data['time_slot'] >= window_start) & (data['time_slot'] < train_end)].copy()
        valid_data = data[data['time_slot'] >= train_end].copy()
        logger.info("训练数据记录数: %d, 验证数据记录数: %d", len(train_data), len(valid_data))
    else:
        train_data = data[data['time_slot'] >= window_start].copy()
        valid_data = pd.DataFrame()
        logger.info("训练数据记录数: %d", len(train_data))

    channels = ['C1', 'C2', 'C3', 'C4', 'C5']
    features = ['hour_sin', 'hour_cos', 'minute_sin', 'minute_cos', 'weekday', 'is_holiday', 'is_peak',
                'dispatch_count', 'single_trip_duration', 'temperature', 'precipitation', 'wind_speed', 'temp_comfort'] + \
               list(enc.get_feature_names_out(['rain_category']))
    models = {}
    valid_results = []
    metrics = {'overall': {}, 'by_channel': {}}

    # 分通道训练
    for channel in channels:
        channel_data = train_data[train_data['channel'] == channel].copy()
        if len(channel_data) < 10:
            logger.warning("通道 %s 训练数据不足: %d", channel, len(channel_data))
            continue

        X_train = channel_data[features].values
        y_train = channel_data['total_flow'].values

        # 时间序列交叉验证
        tscv = TimeSeriesSplit(n_splits=3)
        best_score = float('inf')
        best_model = None
        for train_idx, val_idx in tscv.split(X_train):
            X_cv_train, X_cv_val = X_train[train_idx], X_train[val_idx]
            y_cv_train, y_cv_val = y_train[train_idx], y_train[val_idx]
            model = XGBRegressor(n_estimators=100, max_depth=5, learning_rate=0.1, random_state=42)
            model.fit(X_cv_train, y_cv_train)
            y_pred = model.predict(X_cv_val)
            score = mean_squared_error(y_cv_val, y_pred)
            if score < best_score:
                best_score = score
                best_model = model
        models[channel] = best_model
        logger.info("通道 %s 模型训练完成，最佳 MSE: %.2f", channel, best_score)

        # 验证
        if validate and not valid_data.empty:
            valid_channel_data = valid_data[valid_data['channel'] == channel].copy()
            if valid_channel_data.empty:
                logger.warning("通道 %s 验证数据为空", channel)
                continue
            X_valid = valid_channel_data[features].values
            y_valid = valid_channel_data['total_flow'].values
            y_pred = best_model.predict(X_valid)
            y_pred = np.clip(np.round(y_pred), 0, None)
            valid_channel_data['predicted_flow'] = y_pred
            valid_channel_data['actual_flow'] = y_valid
            valid_results.append(valid_channel_data[['time_slot', 'channel', 'actual_flow', 'predicted_flow']])

            metrics['by_channel'][channel] = {
                'MSE': mean_squared_error(y_valid, y_pred),
                'MAE': mean_absolute_error(y_valid, y_pred),
                'MAPE': mean_absolute_percentage_error(y_valid, y_pred),
                'R2': r2_score(y_valid, y_pred)
            }
            logger.info("通道 %s 验证指标: MSE=%.2f, MAE=%.2f, MAPE=%.2f%%, R2=%.2f",
                        channel, metrics['by_channel'][channel]['MSE'], metrics['by_channel'][channel]['MAE'],
                        metrics['by_channel'][channel]['MAPE'], metrics['by_channel'][channel]['R2'])

    # 总体指标
    if validate and valid_results:
        valid_results_df = pd.concat(valid_results, ignore_index=True)
        metrics['overall'] = {
            'MSE': mean_squared_error(valid_results_df['actual_flow'], valid_results_df['predicted_flow']),
            'MAE': mean_absolute_error(valid_results_df['actual_flow'], valid_results_df['predicted_flow']),
            'MAPE': mean_absolute_percentage_error(valid_results_df['actual_flow'], valid_results_df['predicted_flow']),
            'R2': r2_score(valid_results_df['actual_flow'], valid_results_df['predicted_flow'])
        }
        logger.info("整体验证指标: MSE=%.2f, MAE=%.2f, MAPE=%.2f%%, R2=%.2f",
                    metrics['overall']['MSE'], metrics['overall']['MAE'], metrics['overall']['MAPE'], metrics['overall']['R2'])
    else:
        valid_results_df = pd.DataFrame()

    # 预测未来客流
    now = datetime.now()
    granularity_minutes = {'15min': 15, '30min': 30, '1h': 60}.get(time_granularity, 15)
    future_times = [now + timedelta(minutes=x) for x in [granularity_minutes, granularity_minutes*2, granularity_minutes*4]]
    predictions = []
    for idx, t in enumerate(future_times):
        future_slot = pd.Timestamp(t).replace(second=0, microsecond=0).floor(time_granularity)
        for channel_idx, channel in enumerate(channels, 1):
            future_data = pd.DataFrame({
                'time_slot': [future_slot],
                'hour': [future_slot.hour],
                'minute': [future_slot.minute],
                'hour_sin': [np.sin(2 * np.pi * future_slot.hour / 24)],
                'hour_cos': [np.cos(2 * np.pi * future_slot.hour / 24)],
                'minute_sin': [np.sin(2 * np.pi * future_slot.minute / 60)],
                'minute_cos': [np.cos(2 * np.pi * future_slot.minute / 60)],
                'weekday': [future_slot.weekday()],
                'is_holiday': [1 if future_slot.date() in China(years=datetime.now().year) else 0],
                'is_peak': [1 if future_slot.hour in [7, 8, 17, 18] else 0],
                'dispatch_count': [train_data[train_data['channel'] == channel]['dispatch_count'].mean() if not train_data[train_data['channel'] == channel].empty else 1],
                'single_trip_duration': [train_data[train_data['channel'] == channel]['single_trip_duration'].mean() if not train_data[train_data['channel'] == channel].empty else 10],
                'temperature': [weather_data['temperature'].mean()],
                'precipitation': [weather_data['precipitation'].mean()],
                'wind_speed': [weather_data['wind_speed'].mean()],
                'temp_comfort': [1 if 15 <= weather_data['temperature'].mean() <= 25 else 0]
            })
            for col in enc.get_feature_names_out(['rain_category']):
                # 使用默认值 'no_rain' 如果 weather_data['rain_category'] 不可用
                rain_mode = weather_data['rain_category'].mode()[0] if 'rain_category' in weather_data.columns and not weather_data['rain_category'].isna().all() else 'no_rain'
                future_data[col] = [1 if col == f'rain_category_{rain_mode}' else 0]

            X_future = scaler.transform(future_data[features])
            model = models.get(channel, XGBRegressor(n_estimators=100, max_depth=5, learning_rate=0.1, random_state=42))
            flow_pred = int(np.clip(np.round(model.predict(X_future)[0]), 1, None))

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
            result.append({
                'passengewayIndex': channel_idx,
                'passengewayName': CHANNEL_NAME_MAPPING[channels[channel_idx-1]],
                'instationMin15': 1,
                'instationMin30': 1,
                'instationMin60': 1
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
        data, scaler, enc, weather_data = preprocess_data(trade_data, broadcast_data, schedule_data, weather_data, CONFIG['time_granularity'])
        predictions, _, _ = train_gpr_and_predict(data, scaler, enc, weather_data, CONFIG['window_days'], CONFIG['time_granularity'], validate=False)

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
        data, scaler, enc, weather_data = preprocess_data(trade_data, broadcast_data, schedule_data, weather_data, CONFIG['time_granularity'])
        predictions, valid_results, metrics = train_gpr_and_predict(data, scaler, enc, weather_data, CONFIG['window_days'], CONFIG['time_granularity'], validate=True)

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