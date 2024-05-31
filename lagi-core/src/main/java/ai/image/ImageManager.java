package ai.image;

import ai.common.ModelService;
import ai.common.exception.RRException;
import ai.image.adapter.IImage2TextAdapter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ImageManager {

    private static  final Map<String, IImage2TextAdapter> image2TextAdapter = new ConcurrentHashMap<>();

    public static void registerImage2TextAdapter(String name, IImage2TextAdapter adapter) {
        IImage2TextAdapter tempAdapter = image2TextAdapter.putIfAbsent(name, adapter);
        if (tempAdapter != null) {
            throw new RRException("Adapter " + name + " already exists");
        }
    }

    public static IImage2TextAdapter getImage2TextAdapter(String name) {
        return image2TextAdapter.get(name);
    }

    public static  IImage2TextAdapter getImage2TextAdapter() {
        return image2TextAdapter.entrySet().stream().sorted((entry1, entry2) -> {
            ModelService modelService1 = (ModelService) entry1.getValue();
            Integer priority1 = modelService1.getPriority();
            ModelService modelService2 = (ModelService) entry2.getValue();
            Integer priority2 = modelService2.getPriority();
            if(priority1 != null && priority2 != null) {
                return priority2 - priority1;
            } if(priority1 == null) {
                return 1;
            }
            return -1;
        }).map(Map.Entry::getValue).collect(Collectors.toList()).stream().findAny().orElse(null);
    }
}
