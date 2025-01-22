package com.example.redissizer;

import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificDatumWriter;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class AvroRedisSerializer<T> implements RedisSerializer<T> {

    private final Class<T> type;

    public AvroRedisSerializer(Class<T> type) {
        this.type = type;
    }

    @Override
    public byte[] serialize(T t) throws SerializationException {
        if (t == null) {
            return new byte[0];
        }
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            SpecificDatumWriter<T> datumWriter = new SpecificDatumWriter<>(type);
            BinaryEncoder encoder = EncoderFactory.get().binaryEncoder(outputStream, null);
            datumWriter.write(t, encoder);
            encoder.flush();
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new SerializationException("Serialization failed", e);
        }
    }

    @Override
    public T deserialize(byte[] bytes) throws SerializationException {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes)) {
            SpecificDatumReader<T> datumReader = new SpecificDatumReader<>(type);
            BinaryDecoder decoder = DecoderFactory.get().binaryDecoder(inputStream, null);
            return datumReader.read(null, decoder);
        } catch (Exception e) {
            throw new SerializationException("Deserialization failed", e);
        }
    }
}
