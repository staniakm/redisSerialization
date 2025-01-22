package com.example.redissizer;

import java.io.Serializable;
import java.util.List;

public record KryoPersonList(List<PersonModel> ppl) implements Serializable {
}
