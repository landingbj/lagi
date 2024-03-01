package ai.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class CSVUtils {
	public static List<String> readCsvColumn(String csvPath, int column) {
		List<String> data = new ArrayList<>();
		int i = column - 1;
		File file = new File(csvPath);

		try (FileInputStream fis = new FileInputStream(file); 
				BufferedReader br = new BufferedReader(new InputStreamReader(fis, "UTF-8"))) {
			String line = null;
			while ((line = br.readLine()) != null) {
				String[] strs = line.split(",");
				data.add(strs[i]);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return data;
	}
	
	public static List<String> readCsvColumn(String csvPath, int column, int row, int rowOffset) {
		List<String> data = new ArrayList<>();
		int i = column - 1;
		File file = new File(csvPath);
		int endRow = row + rowOffset;

		try (FileInputStream fis = new FileInputStream(file); 
				BufferedReader br = new BufferedReader(new InputStreamReader(fis, "UTF-8"))) {
			String line = null;
			int k = 1;
			while ((line = br.readLine()) != null) {
				if (k < row || k >= endRow) {
					continue;
				}
				String[] strs = line.split(",");
				data.add(strs[i]);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return data;
	}
	
	public static int countRows(String csvPath) {
		int count = 0;
		File file = new File(csvPath);
		try (FileInputStream fis = new FileInputStream(file); 
				BufferedReader br = new BufferedReader(new InputStreamReader(fis, "UTF-8"))) {
			while (br.readLine() != null) {
				count ++;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return count;
	}
}
