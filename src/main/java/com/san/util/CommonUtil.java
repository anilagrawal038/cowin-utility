package com.san.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class CommonUtil {

	static Logger logger = LoggerFactory.getLogger(CommonUtil.class);

	private static ObjectMapper objectMapper = new ObjectMapper();

	public static void mapProperties(Object sourceObject, Object destObject) {
		if (sourceObject == null || destObject == null) {
			return;
		}
		Class<?> sourceClass = sourceObject.getClass();
		Class<?> destClass = destObject.getClass();
		Method[] methods = sourceClass.getMethods();
		for (Method sourceMethod : methods) {
			String sourceMethodName = sourceMethod.getName();
			String destMethodName = "set" + sourceMethodName.substring(3);
			Method destMethod = null;
			try {
				if (sourceMethodName.startsWith("get")) {
					destMethod = destClass.getMethod(destMethodName, sourceMethod.getReturnType());
				} else if (sourceMethodName.startsWith("is")) {
					destMethodName = "set" + sourceMethodName.substring(2);
					destMethod = destClass.getMethod(destMethodName, sourceMethod.getReturnType());
				}
				if (destMethod != null) {
					destMethod.invoke(destObject, sourceMethod.invoke(sourceObject));
				}
			} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			}
		}
	}

	public static String fetchStringFromInputStream(InputStream inputStream) throws IOException {
		BufferedReader inStreamReader = new BufferedReader(new InputStreamReader(inputStream));
		String inputLine;
		StringBuffer response = new StringBuffer();
		while ((inputLine = inStreamReader.readLine()) != null) {
			response.append(inputLine);
		}
		inputStream.close();
		return response.toString();
	}

	public static InputStream fetchInputStreamFromString(String data) {
		return new ByteArrayInputStream(data.getBytes());
	}

	public static String convertToJsonString(Object object) throws JsonGenerationException, JsonMappingException, IOException {
		String out = null;
		ByteArrayOutputStream byteOutputStream = null;
		try {
			byteOutputStream = new ByteArrayOutputStream();
			objectMapper.writeValue(byteOutputStream, object);
			out = byteOutputStream.toString();
		} finally {
			byteOutputStream.close();
		}
		return out;
	}

	public static byte[] convertToJsonBytes(Object object) throws JsonGenerationException, JsonMappingException, IOException {
		byte[] out = null;
		ByteArrayOutputStream byteOutputStream = null;
		try {
			byteOutputStream = new ByteArrayOutputStream();
			objectMapper.writeValue(byteOutputStream, object);
			out = byteOutputStream.toByteArray();
		} finally {
			byteOutputStream.close();
		}
		return out;
	}

	public static void writeJson(Writer writer, Object object) throws JsonGenerationException, JsonMappingException, IOException {
		try {
			objectMapper.writeValue(writer, object);
		} finally {
			writer.close();
		}
	}

	public static String readStringFromReader(Reader reader) throws JsonParseException, JsonMappingException, IOException {
		StringBuilder stringBuilder = null;
		try {
			BufferedReader br = new BufferedReader(reader);
			String str = null;
			stringBuilder = new StringBuilder();
			while ((str = br.readLine()) != null) {
				System.out.println(str);
				stringBuilder.append(str);
			}
		} finally {
			reader.close();
		}
		return stringBuilder.toString();
	}

	public static Map<String, Object> readJsonMapFromReader(Reader reader) throws JsonParseException, JsonMappingException, IOException {
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			BufferedReader br = new BufferedReader(reader);
			String str = null;
			StringBuilder stringBuilder = new StringBuilder();
			while ((str = br.readLine()) != null) {
				System.out.println(str);
				stringBuilder.append(str);
			}
			map = objectMapper.readValue(stringBuilder.toString(), new TypeReference<HashMap<String, Object>>() {
			});
		} finally {
			reader.close();
		}
		return map;
	}

	public static JsonNode readJsonNodeFromReader(Reader reader) throws JsonProcessingException, IOException {
		JsonNode jsonNode = null;
		try {
			jsonNode = objectMapper.readTree(reader);
		} finally {
			reader.close();
		}
		return jsonNode;
	}

	public static JsonNode fetchJSONResource(String resource) {
		JsonNode jsonNode = null;
		InputStream is = null;
		try {
			is = CommonUtil.class.getClassLoader().getResourceAsStream(resource);
			jsonNode = objectMapper.readTree(is);
			is.close();
		} catch (Exception e) {
		} finally {

		}
		return jsonNode;
	}

	public static <T> T bindJSONToObject(String jsonString, Class<T> clazz) throws JsonParseException, JsonMappingException, IOException {
		T obj = objectMapper.readValue(jsonString, clazz);
		return obj;
	}

	public static Map<String, String> bindJSONToMap(String jsonString) throws JsonParseException, JsonMappingException, IOException {
		TypeFactory factory = TypeFactory.defaultInstance();
		MapType type = factory.constructMapType(HashMap.class, String.class, String.class);
		Map<String, String> obj = objectMapper.readValue(jsonString, type);
		return obj;
	}

	public static <T> T bindXMLToObject(String xmlString, Class<T> clazz) throws JAXBException {
		JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		;
		JAXBElement<T> element = jaxbUnmarshaller.unmarshal(new StreamSource(CommonUtil.fetchInputStreamFromString(xmlString)), clazz);
		T object = element.getValue();
		return object;
	}

	public static String convertToXMLString(Object object) throws JAXBException, IOException {
		String out = null;
		ByteArrayOutputStream byteOutputStream = null;
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(object.getClass());
			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
			// output pretty printed
			// jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			byteOutputStream = new ByteArrayOutputStream();
			jaxbMarshaller.marshal(object, byteOutputStream);
			out = byteOutputStream.toString();
		} finally {
			byteOutputStream.close();
		}
		return out;
	}

	public static void writeXML(Writer writer, Object object) throws JAXBException {
		JAXBContext jaxbContext = JAXBContext.newInstance(object.getClass());
		Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
		// output pretty printed
		// jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		jaxbMarshaller.marshal(object, writer);
	}

	public static byte[] fetchByteArrayOfSerializable(Serializable object) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutput out = null;
		byte[] bytes = null;
		try {
			out = new ObjectOutputStream(bos);
			out.writeObject(object);
			out.flush();
			bytes = bos.toByteArray();
		} finally {
			try {
				bos.close();
			} catch (IOException ex) {
				logger.error("Exception in fetchByteArrayOfSerializable(), exp : ", ex);
			}
		}
		return bytes;
	}

	public static Serializable fetchSerializableFromByteArray(byte[] bytes) throws IOException, ClassNotFoundException {
		ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
		ObjectInput in = null;
		Serializable object = null;
		try {
			in = new ObjectInputStream(bis);
			object = (Serializable) in.readObject();
		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (IOException ex) {
				logger.error("Exception in fetchSerializableFromByteArray(), exp : ", ex);
			}
		}
		return object;
	}

	public static String encodeToBase64String(byte[] bytes) {
		return Base64.getEncoder().encodeToString(bytes);
	}

	public static byte[] decodeFromBase64String(String data) {
		return Base64.getDecoder().decode(data);
	}

	public static Long tryToFetchEntityId(Object obj) {
		Long id = null;
		if (obj != null) {
			try {
				Method getIdMethod = obj.getClass().getMethod("getId");
				if (getIdMethod != null) {
					Object idObj = getIdMethod.invoke(obj);
					if (idObj != null && idObj instanceof Long) {
						id = (Long) idObj;
					}
				}
			} catch (ReflectiveOperationException | SecurityException | IllegalArgumentException e) {
			}
		}
		return id;
	}

	@SuppressWarnings("unchecked")
	public static Object trimProperties(Object sourceObject) {
		Object destObject = null;
		try {
			destObject = sourceObject.getClass().getConstructor().newInstance();
		} catch (Exception exp) {
			return sourceObject;
		}
		Class<?> clazz = sourceObject.getClass();
		Method[] methods = clazz.getMethods();
		for (Method sourceMethod : methods) {
			String sourceMethodName = sourceMethod.getName();
			String destMethodName = "set" + sourceMethodName.substring(3);
			Method destMethod = null;
			try {

				if (sourceMethodName.startsWith("get")) {
					destMethod = clazz.getMethod(destMethodName, sourceMethod.getReturnType());
				} else if (sourceMethodName.startsWith("is")) {
					destMethodName = "set" + sourceMethodName.substring(2);
					destMethod = clazz.getMethod(destMethodName, sourceMethod.getReturnType());
				} else {
					continue;
				}
				Class<?> returnType = sourceMethod.getReturnType();
				Class<?>[] paramTypes = destMethod.getParameterTypes();
				Object valueObject = sourceMethod.invoke(sourceObject);
				String fieldName = destMethodName.substring(3, 4).toLowerCase() + destMethodName.substring(4);
				Field targetField = clazz.getDeclaredField(fieldName);
				if (valueObject == null || targetField == null || paramTypes.length != 1 || !paramTypes[0].equals(returnType)) {
					continue;
				}
				if (returnType.equals(String.class)) {
					String value = (String) valueObject;
					if (value != null) {
						destMethod.invoke(destObject, value.trim());
					}
				} else if (Iterable.class.isAssignableFrom(returnType) && ((ParameterizedType) targetField.getGenericType()).getActualTypeArguments()[0].equals(String.class)) {
					if (Set.class.isAssignableFrom(returnType)) {
						Set<String> values = (Set<String>) valueObject;
						Set<String> newIterable = values.getClass().getConstructor().newInstance();
						for (String value : values) {
							if (value != null) {
								newIterable.add(value.trim());
							}
						}
						destMethod.invoke(destObject, newIterable);
					} else if (List.class.isAssignableFrom(returnType)) {
						List<String> values = (List<String>) valueObject;
						List<String> newIterable = values.getClass().getConstructor().newInstance();
						for (String value : values) {
							if (value != null) {
								newIterable.add(value.trim());
							}
						}
						destMethod.invoke(destObject, newIterable);
					} else if (Queue.class.isAssignableFrom(returnType)) {
						Queue<String> values = (Queue<String>) valueObject;
						Queue<String> newIterable = values.getClass().getConstructor().newInstance();
						for (String value : values) {
							if (value != null) {
								newIterable.add(value.trim());
							}
						}
						destMethod.invoke(destObject, newIterable);
					}
				} else if (Map.class.isAssignableFrom(returnType) && ((ParameterizedType) targetField.getGenericType()).getActualTypeArguments()[0].equals(String.class) && ((ParameterizedType) targetField.getGenericType()).getActualTypeArguments()[1].equals(String.class)) {
					Map<String, String> map = (Map<String, String>) valueObject;
					Map<String, String> newMap = map.getClass().getConstructor().newInstance();
					for (String key : map.keySet()) {
						String value = map.get(key);
						if (value != null) {
							newMap.put(key.trim(), value.trim());
						}
					}
					destMethod.invoke(destObject, newMap);
				} else {
					destMethod.invoke(destObject, valueObject);
				}
			} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | InstantiationException | NoSuchFieldException e) {
			}
		}
		return destObject;
	}

	public static byte[] objectToBytes(Object obj) {
		byte[] data = new byte[] {};
		if (obj == null) {
			return data;
		}
		ByteArrayOutputStream bos = null;
		ObjectOutputStream oos = null;
		try {
			bos = new ByteArrayOutputStream();
			oos = new ObjectOutputStream(bos);
			oos.writeObject(obj);
			oos.flush();
			data = bos.toByteArray();
		} catch (Exception e) {
			logger.info("Exception occurred in CommonsUtil.objectToBytes(), obj : " + obj);
		} finally {
			try {
				oos.close();
			} catch (Exception e) {
			}
			try {
				bos.close();
			} catch (Exception e) {
			}
		}
		return data;
	}

	public static Object bytesToObject(byte[] bytes) {
		Object obj = null;
		if (bytes == null) {
			return obj;
		}
		ByteArrayInputStream bis = null;
		ObjectInputStream ois = null;
		try {
			bis = new ByteArrayInputStream(bytes);
			ois = new ObjectInputStream(bis);
			obj = ois.readObject();
		} catch (Exception e) {
			logger.info("Exception occurred in CommonUtil.bytesToObject(), bytes : " + bytes);
		} finally {
			try {
				ois.close();
			} catch (Exception e) {
			}
			try {
				bis.close();
			} catch (Exception e) {
			}
		}
		return obj;
	}

	public static boolean isWindows() {
		String osName = System.getProperty("os.name");
		if (osName != null && !osName.isEmpty() && osName.toLowerCase().indexOf("windows") > -1) {
			return true;
		}
		return false;
	}

	public static String executeCommand(String shellCommand) {
		String arg1, arg2, line = null, res = null;
		if (isWindows()) {
			arg1 = "cmd.exe";
			arg2 = "/C";
		} else {
			arg1 = "bash";
			arg2 = "-c";
		}

		ProcessBuilder startServiceProcessBuilder = new ProcessBuilder(arg1, arg2, shellCommand);
		Process p;
		try {
			p = startServiceProcessBuilder.start();
			p.waitFor(5, TimeUnit.SECONDS);
		} catch (IOException | InterruptedException e) {
			logger.error("Exception occurred while executing command : " + shellCommand + "", e);
			return res;
		}

		InputStreamReader tempReader = new InputStreamReader(new BufferedInputStream(p.getInputStream()));
		final BufferedReader reader = new BufferedReader(tempReader);
		InputStreamReader tempErrReader = new InputStreamReader(new BufferedInputStream(p.getErrorStream()));
		final BufferedReader errReader = new BufferedReader(tempErrReader);

		try {
			logger.info("Executed command : " + shellCommand + " :: Console Output : \n");
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				if (!line.isEmpty()) {
					res = line;
					logger.info(line);
				}

			}
		} catch (IOException e) {
			logger.info("Executed command : " + shellCommand + " :: Console Output Line : " + line);
			logger.trace("Exception occurred while executing command : " + shellCommand + " ", e);
		}

		try {
			logger.info("Executed command : " + shellCommand + " :: Error Output : \n");
			while ((line = errReader.readLine()) != null) {
				line = line.trim();
				if (!line.isEmpty()) {
					logger.error(line);
				}

			}
		} catch (IOException e) {
			logger.error("Exception occurred while executing command : " + shellCommand + "", e);
		}
		try {
			reader.close();
		} catch (Exception e) {
		}
		try {
			tempReader.close();
		} catch (Exception e) {
		}
		try {
			errReader.close();
		} catch (Exception e) {
		}
		try {
			tempErrReader.close();
		} catch (Exception e) {
		}
		return res;
	}

	// Find output messages at index 0 & error messages at index 1 in returned list
	public static List<List<String>> executeCommandWithOutput(String shellCommand) {
		String arg1, arg2, line = null;
		List<String> outputLines = new ArrayList<String>();
		List<String> errLines = new ArrayList<String>();
		if (isWindows()) {
			arg1 = "cmd.exe";
			arg2 = "/C";
		} else {
			arg1 = "bash";
			arg2 = "-c";
		}
		logger.debug("Executing command : " + shellCommand);
		ProcessBuilder startServiceProcessBuilder = new ProcessBuilder(arg1, arg2, shellCommand);
		Process p;
		try {
			p = startServiceProcessBuilder.start();
			p.waitFor(3, TimeUnit.SECONDS);
		} catch (IOException | InterruptedException e) {
			logger.error("Exception occurred while executing command : " + shellCommand + "", e);
			return null;
		}

		InputStreamReader tempReader = new InputStreamReader(new BufferedInputStream(p.getInputStream()));
		final BufferedReader reader = new BufferedReader(tempReader);
		InputStreamReader tempErrReader = new InputStreamReader(new BufferedInputStream(p.getErrorStream()));
		final BufferedReader errReader = new BufferedReader(tempErrReader);

		try {
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				if (!line.isEmpty()) {
					outputLines.add(line);
				}
			}
		} catch (IOException e) {
			logger.info("Executed command : " + shellCommand + " :: Console Output Line : " + line);
			logger.trace("Exception occurred while executing command : " + shellCommand + " ", e);
		}

		try {
			while ((line = errReader.readLine()) != null) {
				line = line.trim();
				if (!line.isEmpty()) {
					errLines.add(line);
				}
			}
		} catch (IOException e) {
			logger.trace("Exception occurred while executing command : " + shellCommand + "", e);
		}
		try {
			reader.close();
		} catch (Exception e) {
		}
		try {
			tempReader.close();
		} catch (Exception e) {
		}
		try {
			errReader.close();
		} catch (Exception e) {
		}
		try {
			tempErrReader.close();
		} catch (Exception e) {
		}
		return Arrays.asList(outputLines, errLines);
	}

}
