package com.mai.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;

import java.io.*;

public class SerializationHelper<T extends Serializable> {

    Class<T> serialazationType;

    public SerializationHelper(Class<T> serialazationType) {
        this.serialazationType = serialazationType;
    }

    public SerializationHelper() {
    }

    private Logger log = Logger.getLogger(getClass());

    ObjectMapper mapper = new ObjectMapper();


    /*
      Необходимо десереализовать объект из файла по указанному пути
     */
    public T loadFromFile(String path) {
        try (ObjectInputStream input = new ObjectInputStream(new FileInputStream(path))) {
            return (T) input.readObject();
        } catch (IOException | ClassNotFoundException e) {
            log.error(e);
            e.printStackTrace();
        }
        return null;
    }

    /*
      Необходимо сохранить сереализованный объект в файл по указанному пути
     */
    public boolean saveToFile(String path, T toSave) {
        try (ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(path))) {
            output.writeObject(toSave);
            output.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public String convertToJsonString(T toConvert) throws JsonProcessingException {
        return mapper.writeValueAsString(toConvert);
    }

    public void writeJsonToStream(OutputStream output, T toWrite) {
        try {
            mapper.writeValue(output, toWrite);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public T parseJson(String json) {
        try {
            return mapper.readValue(json, serialazationType);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
