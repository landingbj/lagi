package ai.agent.carbus.util;

public class CoordinateConverter {
    // 地球半径(米)
    private static final double EARTH_RADIUS = 6378137;
    // 中国范围判断常量
    private static final double MIN_LNG = 72.004;
    private static final double MAX_LNG = 137.8347;
    private static final double MIN_LAT = 0.8293;
    private static final double MAX_LAT = 55.8271;

    /**
     * 判断坐标是否在中国范围内
     * @param lng 经度
     * @param lat 纬度
     * @return 是否在中国范围内
     */
    public static boolean outOfChina(double lng, double lat) {
        return (lng < MIN_LNG || lng > MAX_LNG) || (lat < MIN_LAT || lat > MAX_LAT);
    }

    /**
     * 转换纬度的辅助函数
     */
    private static double transformLat(double x, double y) {
        double ret = -100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y + 0.2 * Math.sqrt(Math.abs(x));
        ret += (20.0 * Math.sin(6.0 * x * Math.PI) + 20.0 * Math.sin(2.0 * x * Math.PI)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(y * Math.PI) + 40.0 * Math.sin(y / 3.0 * Math.PI)) * 2.0 / 3.0;
        ret += (160.0 * Math.sin(y / 12.0 * Math.PI) + 320 * Math.sin(y * Math.PI / 30.0)) * 2.0 / 3.0;
        return ret;
    }

    /**
     * 转换经度的辅助函数
     */
    private static double transformLng(double x, double y) {
        double ret = 300.0 + x + 2.0 * y + 0.1 * x * x + 0.1 * x * y + 0.1 * Math.sqrt(Math.abs(x));
        ret += (20.0 * Math.sin(6.0 * x * Math.PI) + 20.0 * Math.sin(2.0 * x * Math.PI)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(x * Math.PI) + 40.0 * Math.sin(x / 3.0 * Math.PI)) * 2.0 / 3.0;
        ret += (150.0 * Math.sin(x / 12.0 * Math.PI) + 300.0 * Math.sin(x / 30.0 * Math.PI)) * 2.0 / 3.0;
        return ret;
    }

    /**
     * 计算WGS84坐标到GCJ02(高德)坐标的偏移量
     */
    private static double[] delta(double lng, double lat) {
        double dLat = transformLat(lng - 105.0, lat - 35.0);
        double dLng = transformLng(lng - 105.0, lat - 35.0);
        double radLat = lat / 180.0 * Math.PI;
        double magic = Math.sin(radLat);
        magic = 1 - 0.006693421622965943 * magic * magic;
        double sqrtMagic = Math.sqrt(magic);
        dLat = (dLat * 180.0) / ((EARTH_RADIUS * (1 - 0.006693421622965943)) / (magic * sqrtMagic) * Math.PI);
        dLng = (dLng * 180.0) / (EARTH_RADIUS / sqrtMagic * Math.cos(radLat) * Math.PI);
        return new double[]{dLat, dLng};
    }

    /**
     * WGS84坐标转换为GCJ02(高德)坐标
     * @param wgsLng WGS84经度
     * @param wgsLat WGS84纬度
     * @return 包含GCJ02经度和纬度的数组 [lng, lat]
     */
    public static double[] wgs84ToGcj02(double wgsLng, double wgsLat) {
        if (outOfChina(wgsLng, wgsLat)) {
            return new double[]{wgsLng, wgsLat};
        }
        double[] d = delta(wgsLng, wgsLat);
        return new double[]{wgsLng + d[1], wgsLat + d[0]};
    }

    /**
     * GCJ02(高德)坐标转换为WGS84坐标
     * @param gcjLng GCJ02经度
     * @param gcjLat GCJ02纬度
     * @return 包含WGS84经度和纬度的数组 [lng, lat]
     */
    public static double[] gcj02ToWgs84(double gcjLng, double gcjLat) {
        if (outOfChina(gcjLng, gcjLat)) {
            return new double[]{gcjLng, gcjLat};
        }
        double[] d = delta(gcjLng, gcjLat);
        return new double[]{gcjLng - d[1], gcjLat - d[0]};
    }

    /**
     * GCJ02(高德)坐标转换为BD-09(百度)坐标
     * @param gcjLng GCJ02经度
     * @param gcjLat GCJ02纬度
     * @return 包含BD-09经度和纬度的数组 [lng, lat]
     */
    public static double[] gcj02ToBd09(double gcjLng, double gcjLat) {
        double x = gcjLng, y = gcjLat;
        double z = Math.sqrt(x * x + y * y) + 0.00002 * Math.sin(y * Math.PI);
        double theta = Math.atan2(y, x) + 0.000003 * Math.cos(x * Math.PI);
        double bdLng = z * Math.cos(theta) + 0.0065;
        double bdLat = z * Math.sin(theta) + 0.006;
        return new double[]{bdLng, bdLat};
    }

    /**
     * BD-09(百度)坐标转换为GCJ02(高德)坐标
     * @param bdLng BD-09经度
     * @param bdLat BD-09纬度
     * @return 包含GCJ02经度和纬度的数组 [lng, lat]
     */
    public static double[] bd09ToGcj02(double bdLng, double bdLat) {
        double x = bdLng - 0.0065;
        double y = bdLat - 0.006;
        double z = Math.sqrt(x * x + y * y) - 0.00002 * Math.sin(y * Math.PI);
        double theta = Math.atan2(y, x) - 0.000003 * Math.cos(x * Math.PI);
        double gcjLng = z * Math.cos(theta);
        double gcjLat = z * Math.sin(theta);
        return new double[]{gcjLng, gcjLat};
    }

    /**
     * WGS84坐标转换为BD-09(百度)坐标
     * @param wgsLng WGS84经度
     * @param wgsLat WGS84纬度
     * @return 包含BD-09经度和纬度的数组 [lng, lat]
     */
    public static double[] wgs84ToBd09(double wgsLng, double wgsLat) {
        double[] gcj = wgs84ToGcj02(wgsLng, wgsLat);
        return gcj02ToBd09(gcj[0], gcj[1]);
    }

    /**
     * BD-09(百度)坐标转换为WGS84坐标
     * @param bdLng BD-09经度
     * @param bdLat BD-09纬度
     * @return 包含WGS84经度和纬度的数组 [lng, lat]
     */
    public static double[] bd09ToWgs84(double bdLng, double bdLat) {
        double[] gcj = bd09ToGcj02(bdLng, bdLat);
        return gcj02ToWgs84(gcj[0], gcj[1]);
    }
}