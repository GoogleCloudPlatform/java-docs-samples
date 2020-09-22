package com.example.cloudrun;

import java.sql.Timestamp;

import edu.umd.cs.findbugs.annotations.NonNull;
import lombok.Data;

@Data
public class Vote {
    @NonNull private String uid;
    @NonNull private String candidate;
    @NonNull private Timestamp timeCast;
}
