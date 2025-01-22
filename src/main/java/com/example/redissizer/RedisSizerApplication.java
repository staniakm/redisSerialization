package com.example.redissizer;

import com.example.serialization.model.Person;
import com.example.serialization.model.PersonList;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.IntStream;


@Slf4j
@SpringBootApplication
public class RedisSizerApplication implements CommandLineRunner {
    private final Logger logger = Logger.getLogger("Main");

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private SerializerTest serializerTest;

    @Autowired
    private RedisMemoryUsage memoryUsage;

    public static void main(String[] args) {
        SpringApplication.run(RedisSizerApplication.class, args);
    }


    @Override
    public void run(String... args) {

        PersonModel person = new PersonModel("1", "John Doe", 30);
        var ppl = createLit();
        var packedList = new KryoPersonList(ppl);

        //  JSON
        serializerTest.testSerialization("jsonKey2", person, new GenericJackson2JsonRedisSerializer());
        logger.info("JSON Memory Usage: " + memoryUsage.getMemoryUsage("jsonKey2") + " bytes");

        serializerTest.testSerialization("jsonKeyList", packedList, new GenericJackson2JsonRedisSerializer());
        logger.info("JSON Memory Usage: " + memoryUsage.getMemoryUsage("jsonKeyList") + " bytes");

        //  JDK
        serializerTest.testSerialization("jdkKey2", person, new JdkSerializationRedisSerializer());
        logger.info("JDK Memory Usage: " + getMemoryUsage("jdkKey2") + " bytes");

        serializerTest.testSerialization("jdkKeyList", packedList, new JdkSerializationRedisSerializer());
        logger.info("JDK Memory Usage: " + getMemoryUsage("jdkKeyList") + " bytes");

        //  Kryo
        serializerTest.testSerialization("kryoKey", person, new KryoRedisSerializer<>(PersonModel.class));
        logger.info("Kryo Memory Usage: " + getMemoryUsage("kryoKey") + " bytes");
        serializerTest.testSerialization("kryoKeyList", packedList, new KryoRedisSerializer<>(KryoPersonList.class));
        logger.info("Kryo Memory Usage: " + getMemoryUsage("kryoKeyList") + " bytes");

        //   Avro
        var personAvro = new Person(person.getId(), person.getName(), person.getAge());
        serializerTest.testSerialization("avroKey", personAvro, new AvroRedisSerializer<>(Person.class));
        logger.info("Avro Memory Usage: " + getMemoryUsage("avroKey") + " bytes");
        var avroPpl = toAvro(ppl);
        serializerTest.testSerialization("avroKeyList", avroPpl, new AvroRedisSerializer<>(PersonList.class));
        logger.info("Avro Memory Usage: " + getMemoryUsage("avroKeyList") + " bytes");

        com.example.redissizer.protobuf.Person protoPerson = com.example.redissizer.protobuf.Person.newBuilder().setId(person.getId())
                .setName(person.getName())
                .setAge(person.getAge()).build();
        serializerTest.testSerialization("protoKey", protoPerson, new ProtobufRedisSerializer<>(com.example.redissizer.protobuf.Person.getDefaultInstance()));
        logger.info("Proto Memory Usage: " + getMemoryUsage("protoKey") + " bytes");
        var protoPpl = toProto(ppl);
        serializerTest.testSerialization("protoKeyList", protoPpl, new ProtobufRedisSerializer<>(com.example.redissizer.protobuf.PersonList.getDefaultInstance()));
        logger.info("Proto Memory Usage: " + getMemoryUsage("protoKeyList") + " bytes");


    }

    private com.example.redissizer.protobuf.PersonList toProto(List<PersonModel> ppl) {
        var persons = com.example.redissizer.protobuf.PersonList.newBuilder();
        ppl.stream().map(p -> com.example.redissizer.protobuf.Person.newBuilder().setId(p.getId())
                        .setName(p.getName())
                        .setAge(p.getAge()).build())
                .forEach(persons::addPersons);
        return persons.build();
    }

    private PersonList toAvro(List<PersonModel> ppl) {

        var list = ppl
                .stream().map(p -> new Person(p.getId(), p.getName(), p.getAge()))
                .toList();
        var p = new PersonList();
        p.setPersons(list);
        return p;
    }

    private List<PersonModel> createLit() {
        return IntStream.rangeClosed(1, 100)
                .mapToObj(n -> new PersonModel("" + n, "John Doe" + n, 30 + n))
                .toList();
    }

    private Object getMemoryUsage(String key) {
        String script = "return redis.pcall('MEMORY', 'USAGE', KEYS[1])";

        return redisTemplate
                .executePipelined(
                        (RedisCallback<Object>)
                                connection -> {
                                    connection.eval(
                                            script.getBytes(StandardCharsets.UTF_8),
                                            ReturnType.INTEGER,
                                            1,
                                            key.getBytes(StandardCharsets.UTF_8));
                                    return null;
                                })
                .get(0);
    }
}
