package com.example.redissizer;

import com.google.protobuf.Message;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

public class ProtobufRedisSerializer<T extends Message> implements RedisSerializer<T> {

    private final T defaultInstance;

    public ProtobufRedisSerializer(T defaultInstance) {
        this.defaultInstance = defaultInstance;
    }

    @Override
    public byte[] serialize(T t) throws SerializationException {
        return (t == null) ? new byte[0] : t.toByteArray();
    }

    @Override
    public T deserialize(byte[] bytes) throws SerializationException {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        try {
            return (T) defaultInstance.getParserForType().parseFrom(bytes);
        } catch (Exception e) {
            throw new SerializationException("Protobuf deserialization error", e);
        }
    }
}
