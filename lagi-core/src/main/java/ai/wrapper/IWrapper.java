package ai.wrapper;

import ai.common.pojo.Driver;

import java.util.List;

public interface IWrapper {
    List<Driver> getDrivers(String model);
}
