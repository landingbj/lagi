package ai.migrate;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import ai.dao.store.NodeRepository;
import ai.vertex.BaseVertex;

public class QueryAllParentVertices {
	private static String dataPath = "D:/Test/misc/";
	
	private static String inputFile  = "3852.txt";
	private static String outputFile = "3852_1.csv";
	private static int BATCH_SIZE = 10;
	
	private static NodeRepository nodeRepository = new NodeRepository();

	public static void main(String[] args) {
		String inputfilePath  = dataPath + inputFile;;
		String outputfilePath = dataPath + outputFile;
		
		if (args.length == 2) {
			inputfilePath = args[0];
			outputfilePath = args[1];
		}

		List<Integer> uidList = getIdFromFile(inputfilePath);
		List<List<Integer>> partitions = new ArrayList<>();
		for (int i = 0; i < uidList.size(); i += BATCH_SIZE) {
			partitions.add(uidList.subList(i, Math.min(i + BATCH_SIZE, uidList.size())));
		}

		for (List<Integer> tmpList : partitions) {
			List<BaseVertex> hybirdList = nodeRepository.getUpperHybird(tmpList);
			for (BaseVertex vertex : hybirdList) {
				String line = vertex.getUid() + "," + vertex.getSubIndex() + "," + vertex.getSubAspectJoin() + "," + vertex.getSubId() + "," + vertex.getName();
				writeToCsvFile(outputfilePath, line);
			}
		}
	}

	private static void writeToCsvFile(String filePath, String content) {
	    FileWriter fw;
		try {
			fw = new FileWriter(filePath, true);
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(content);
			bw.newLine();
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static List<Integer> getIdFromFile(String filePath) {
		String content = "";
		try {
			content = new String(Files.readAllBytes(Paths.get(filePath)), StandardCharsets.UTF_8);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return idToIntList(content);
	}

	private static List<Integer> idToIntList(String ids) {
		String[] idArray = ids.split(",");
		List<Integer> idList = new ArrayList<>();
		for (String idStr : idArray) {
			idStr = idStr.trim();
			if (!idStr.equals("")) {
				idList.add(Integer.parseInt(idStr));
			}
		}
		return idList;
	}
}
