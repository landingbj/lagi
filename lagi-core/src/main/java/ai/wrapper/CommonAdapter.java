package ai.wrapper;

import ai.common.pojo.Driver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommonAdapter implements IWrapper {
    @Override
    public List<Driver> getDrivers(String model) {
        return initDrivers(model);
    }

    protected Map<String, String> getModelDriverMap() {
        return null;
    }

    private List<Driver> initDrivers(String modelStr) {
        Map<String, String> modelMap = splitModels(modelStr);
        List<Driver> drivers = new ArrayList<>();
        for (Map.Entry<String, String> entry : modelMap.entrySet()) {
            String driver = entry.getKey();
            String model = entry.getValue();
            drivers.add(initDriver(model, driver));
        }
        return drivers;
    }

    private Map<String, String> splitModels(String model) {
        String[] models = model.replace(" ", "").split(",");
        Map<String, String> driverMap = getModelDriverMap();
        Map<String, List<String>> groupedItems = new HashMap<>();
        for (String m : models) {
            String driver = driverMap.get(m);
            groupedItems.computeIfAbsent(driver, k -> new ArrayList<>()).add(m);
        }
        Map<String, String> result = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : groupedItems.entrySet()) {
            result.put(entry.getKey(), String.join(",", entry.getValue()));
        }
        return result;
    }

    private Driver initDriver(String model, String driver) {
        return Driver.builder()
                .model(model)
                .driver(driver)
                .build();
    }
}
