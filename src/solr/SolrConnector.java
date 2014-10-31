package solr;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * Created by daniel on 2014-04-28.
 * 
 * Call SolrConnector.push(...) to update Solr with data.
 * 
 * Requires Google Gson.
 * 
 */
public class SolrConnector {
	private static final boolean DEBUG = false;
	private static final String SOLR_UPDATE_URL = "http://localhost:8983/solr/images/update/json?commit=true";
	private static int ID_COUNTER = 0;

	private JsonArray documents;
	private int BUFFER_MAX = 100;

	private Object lock;

	public SolrConnector() {
		documents = new JsonArray();
		lock = new Object();

		new Thread(new Worker(lock)).start();
	}

	/**
	 * Add a document to be pushed to Solr
	 * 
	 * @param documentMap
	 *            - A map consisting of key/value-pairs with information turned into
	 *            a document.
	 * 
	 * @return
	 */
	public void push(Map<String, ?> documentMap) {
		synchronized (lock) {
			documents.add(createJson(documentMap));

			if (documents.size() > BUFFER_MAX) {
				lock.notify();
			}
		}
	}

	/**
	 * Add a list of documents to be pushed to Solr
	 * 
	 * @param documentMapList
	 *            - A map consisting of key/value-pairs with information turned into
	 *            a document.
	 * 
	 * @return
	 */
	public void push(List<Map<String, ?>> documentMapList) {
		for (Map<String, ?> data : documentMapList) {
			push(data);
		}
	}

	private JsonObject createJson(Map<String, ?> data) {
		JsonObject jsonDocument = new JsonObject();
		jsonDocument.add("id", new JsonPrimitive(Integer.toString(ID_COUNTER)));
		ID_COUNTER++;

		for (String key : data.keySet()) {
			Object value = data.get(key);
			JsonElement jsonValue;
			if (value instanceof List) {
				JsonArray array = new JsonArray();
				List<?> list = (List<?>) value;
				for (Object v : list) {
					array.add(getPrimitiveValue(v));
				}

				jsonValue = array;
			} else {
				key = getKey(key, value.getClass());
				jsonValue = getPrimitiveValue(value);
			}

			if (jsonValue != null) {
				jsonDocument.add(key, jsonValue);
			} else {
				System.err.println("Unknown data type of key " + key + " with value " + value.toString());
			}
		}

		return jsonDocument;
	}

	private String getKey(String key, Class type) {
		String extension = "";
		/*if(type.equals(Integer.class)) {
		    extension = "_i";
		} else if(type.equals(String.class)) {
		    extension = "_s";
		} else if(type.equals(Double.class)) {
		    extension = "_d";
		} else if(type.equals(Boolean.class)) {
		    extension = "_b";
		}
		*/
		key = key + extension;

		return key;
	}

	private JsonPrimitive getPrimitiveValue(Object value) {
		JsonPrimitive jsonValue = null;
		if (value instanceof String) {
			jsonValue = new JsonPrimitive((String) value);
		} else if (value instanceof Integer) {
			jsonValue = new JsonPrimitive((Number) value);
		} else if (value instanceof Number) {
			jsonValue = new JsonPrimitive((Number) value);
		} else if (value instanceof Boolean) {
			jsonValue = new JsonPrimitive((Boolean) value);
		}

		return jsonValue;
	}

	public JsonArray getBufferAndEmpty() {
		JsonArray docs = documents;
		synchronized (lock) {
			documents = new JsonArray();
		}

		return docs;
	}

	private class Worker implements Runnable {
		private Object lock;

		public Worker(Object lock) {
			this.lock = lock;
		}

		@Override
		public void run() {
			while (true) {
				synchronized (lock) {
					try {
						lock.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

				JsonArray documents = getBufferAndEmpty();

				try {
					URL url = new URL(SOLR_UPDATE_URL);
					HttpURLConnection connection = (HttpURLConnection) url.openConnection();
					connection.setRequestMethod("POST");
					connection.setDoOutput(true);
					connection.setRequestProperty("Content-Type", "application/json");

					OutputStream out = connection.getOutputStream();
					try {
						OutputStreamWriter writer = new OutputStreamWriter(out);
						writer.write(documents.toString());
						writer.flush();
						writer.close();
					} catch (IOException e) {
						e.printStackTrace();
					} finally {
						if (out != null) {
							out.close();
						}
					}

					connection.getResponseCode(); // Magic line, do not remove

					if (DEBUG) {
						System.err.println("---Solr Update Results---");
						System.err.println(connection.getResponseCode());
						System.err.println(connection.getResponseMessage());
						System.err.println();
					}

					connection.disconnect();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
