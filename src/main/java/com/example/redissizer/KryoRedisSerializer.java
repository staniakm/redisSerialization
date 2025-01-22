package com.example.redissizer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class KryoRedisSerializer<T> implements RedisSerializer<T> {

    private final ThreadLocal<Kryo> kryo = ThreadLocal.withInitial(() -> {
        Kryo k = new Kryo();
        k.setRegistrationRequired(false);
//        k.register(Person.class);
//        k.register(PersonModel.class);
//        k.register(java.util.ImmutableCollections.ListN.class);
        return k;
    });
    private final Class<T> type;

    public KryoRedisSerializer(Class<T> type) {
        this.type = type;
    }

    @Override
    public byte[] serialize(T t) throws SerializationException {
        if (t == null) {
            return new byte[0];
        }
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             Output output = new Output(byteArrayOutputStream)) {
            kryo.get().writeObject(output, t);
            output.close();
            return byteArrayOutputStream.toByteArray();
        } catch (Exception e) {
            throw new SerializationException("Serialization failed", e);
        }
    }

    @Override
    public T deserialize(byte[] bytes) throws SerializationException {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
             Input input = new Input(byteArrayInputStream)) {
            return kryo.get().readObject(input, type);
        } catch (Exception e) {
            throw new SerializationException("Deserialization failed", e);
        }
    }
}
