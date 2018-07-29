package com.jsontoschema.converter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;
import java.util.stream.Stream;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;


/**
 * A program to convert a json object to json schema.
 * 
 * @author Daoud Shaheen [28-7-2018]
 *
 */
public class App {

	public static final String TYPE = "\n\"type\": ";
	public static void main(String[] args) {

		//get file name from cmd
		if (args.length > 0) {
			String fileName = args[0];
			//check if the file exists
			if(Files.exists( Paths.get(fileName), LinkOption.NOFOLLOW_LINKS)) {
				//read json from file provided
				String jsonInString = readFile(fileName);
				//convert json string to Object
				Object json = new JSONTokener(jsonInString).nextValue();
				//Convert Json object to a valid schema and write the result to a file
				String outputFileName = writeToFile(fileName, new JSONObject(convertJsonToSchema(json)).toString(2));

				if(outputFileName != null) {
					System.out.println("Converted Successfully ! Please check the result in " + outputFileName);
				}
			} else {
				System.err.println("The File you are trying to convert does not exists !!"); 
			}
		} else {
			System.err.println("Please insert the input file name!!");
		}

	}

	/**
	 * This method is the start point for the program
	 * @param jsonObject
	 * @return
	 */
	public static String convertJsonToSchema(Object jsonObject) {
		StringBuilder schema = new StringBuilder("{");
		schema.append(fromObjectToSchema(jsonObject));
		schema.append("\n}");
		return schema.toString();

	}

	/**
	 * This method is used to go over Json object keys and determine the type of the value
	 * @param jsonObject
	 * @return
	 */
	private static String fromJsonKeysToSchema(JSONObject jsonObject) {
		StringBuilder schema = new StringBuilder();
		int index = 0;
		for (String key : jsonObject.keySet()) {
			schema.append("\n\"").append(key).append("\": {");
			schema.append(fromObjectToSchema(jsonObject.opt(key)));
			schema.append("\n}");
			index++;
			if(index < jsonObject.keySet().size()) {
				schema.append(",");
			} 
		}
		return schema.toString();
	}

	/**
	 * Convert object to related schema object
	 * @param obj
	 * @return
	 */
	private static String fromObjectToSchema(Object obj) {
		StringBuilder schema = new StringBuilder();

		if(obj instanceof Boolean) {
			schema.append(TYPE).append("\"boolean\"");
		} else if(obj instanceof Double) {
			schema.append(TYPE).append("\"number\"");
		}
		else if(obj instanceof Integer || obj instanceof Long) {
			schema.append(TYPE).append("\"integer\"");
		}

		else if(obj instanceof String) {
			schema.append(TYPE).append("\"string\"");
		}
		else if(obj instanceof JSONArray) {
			schema.append(TYPE).append("\"array\" ,");
			JSONArray array = (JSONArray) obj;
			if(array.length() > 0) {
				schema.append("\n\"items\" : {");


				schema.append(fromObjectToSchema(array.get(0)));

				schema.append("\n}");
			}
		}
		else if(obj instanceof JSONObject) {
			schema.append(TYPE).append("\"object\",");
			schema.append("\n\"properties\":{");
			schema.append(fromJsonKeysToSchema((JSONObject)obj));
			schema.append("\n}");
		} else {
			schema.append(TYPE).append("\"null\"");
		}
		return schema.toString();
	}

	/**
	 * Method used to read a file 
	 * @param filePath
	 * @return
	 */
	private static String readFile(String filePath){

		StringBuilder contentBuilder = new StringBuilder();

		try (Stream<String> stream = Files.lines( Paths.get(filePath), StandardCharsets.UTF_8))
		{
			stream.forEach(s -> contentBuilder.append(s));
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		return contentBuilder.toString();
	}

	/**
	 *  Genrate output file name from input filename plus '_schema.json'
	 * @param inputFileName
	 * @return
	 */
	private static String generateOutputFileName(String inputFileName) {
		if(inputFileName.contains(".")) {
			inputFileName = inputFileName.split("\\.")[0];
		}
		return inputFileName + "_schema.json";
	}

	/**
	 * Write output to a file
	 * @param fileName
	 * @param content
	 */
	private static String  writeToFile(String fileName, String content) {
		String outputFileName = generateOutputFileName(fileName);
		try {
			Files.write(Paths.get(outputFileName), content.getBytes());
			return outputFileName;
		} catch (IOException e) {
			System.err.println("Something went wrong please try again !!");
		}
		return null;
	}
}
