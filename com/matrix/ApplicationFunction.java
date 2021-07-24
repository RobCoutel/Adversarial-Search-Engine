package com.matrix;

import java.io.Serializable;

public interface ApplicationFunction extends Serializable {
    double applyFunc(double x, double y);
}
