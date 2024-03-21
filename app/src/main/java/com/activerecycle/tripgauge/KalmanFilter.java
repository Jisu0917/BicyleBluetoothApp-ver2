package com.activerecycle.tripgauge;

public class KalmanFilter {
    private double processNoise; // 프로세스 노이즈 (Q)
    private double measurementNoise; // 측정 노이즈 (R)
    private double kalmanGain; // 칼만 이득

    private double estimatedValue; // 추정값
    private double errorCovariance; // 오차 공분산

    public KalmanFilter(double processNoise, double measurementNoise) {
        this.processNoise = processNoise;
        this.measurementNoise = measurementNoise;
        this.estimatedValue = 0;
        this.errorCovariance = 1;
    }

    public double[] applyFilter(double measuredValue) {
        // 예측 단계
        double predictedValue = estimatedValue;
        double predictedErrorCovariance = errorCovariance + processNoise;

        // 갱신 단계
        kalmanGain = predictedErrorCovariance / (predictedErrorCovariance + measurementNoise);
        estimatedValue = predictedValue + kalmanGain * (measuredValue - predictedValue);
        errorCovariance = (1 - kalmanGain) * predictedErrorCovariance;

        return new double[]{estimatedValue, errorCovariance};
    }
}
