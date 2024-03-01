package ai.migrate;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import ai.dao.store.EdgeRepository;
import ai.edge.impl.EdgeNeighbour;

public class QueryAllLinkedVertices {
	
	private static String dataPath = "D:/Test/misc/";
	
	private static String inputFile  = "3852.txt";
	private static String outputFile = "3852_2.csv";
	private static String aspectName = "ai_aspect_totality_subtype";
	
	private static EdgeRepository edgeRepository = new EdgeRepository();

	public static void main(String[] args) {
		
		String inputfilePath  = dataPath + inputFile;
		String outputfilePath = dataPath + outputFile;
		String aspectTable    = aspectName;
		
		boolean recursiveFlag = true;
		
		if (args.length == 3) {
			inputfilePath  = args[0];
			outputfilePath = args[1];
			aspectTable    = args[2];
		} 
		else if (args.length == 4) {
			if (args[0].equals("-R")) {
				recursiveFlag = true;
			}
			inputfilePath = args[1];
			outputfilePath = args[2];
			aspectTable = args[3];
		}
		
		List<Integer> uidList = getIdFromFile(inputfilePath);
		List<EdgeNeighbour> edgeNeighbourList = edgeRepository.getEdgeNeighbour(uidList, aspectTable, recursiveFlag);
		for (EdgeNeighbour edgeNeighbour : edgeNeighbourList) {
			writeToCsvFile(outputfilePath, toCsvRow(edgeNeighbour));
		}
	}

	private static String toCsvRow(EdgeNeighbour edgeNeighbour) {
		String neighbours = edgeNeighbour.getNeighbourList().toString();
		neighbours = neighbours.replace("[", "");
		neighbours = neighbours.replace(",", "");
		neighbours = neighbours.replace("]", "");
		return edgeNeighbour.getUid() + "," + neighbours;
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
