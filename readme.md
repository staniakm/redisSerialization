Different types of redis value serializations and memory that is used

JSON Memory Usage: 152 bytes  
JSON Memory Usage: 10296 bytes  
JDK Memory Usage: 184 bytes  
JDK Memory Usage: 3128 bytes  
Kryo Memory Usage: 72 bytes  
Kryo Memory Usage: 1592 bytes  
Avro Memory Usage: 72 bytes  
Avro Memory Usage: 1848 bytes  

Measurement is made using jedis command and also redis template command

Kryo requires models to be registered. 

Avro requires avro schema for each serialized type. 