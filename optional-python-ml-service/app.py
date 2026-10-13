"""Optional ML microservice for the Sales Forecasting Dashboard.
POST /predict  {"values": [monthly numbers...], "months": 6}  ->  {"predictions": [...]}
Uses scikit-learn Linear Regression. To enable it, set in the backend's application.properties:
    forecast.ml.url=http://localhost:5000/predict
"""
from flask import Flask, jsonify, request
from flask_cors import CORS
import numpy as np
from sklearn.linear_model import LinearRegression

app = Flask(__name__)
CORS(app)


@app.post("/predict")
def predict():
    data = request.get_json(force=True)
    values = np.array(data.get("values", []), dtype=float)
    months = int(data.get("months", 6))
    n = len(values)

    if n == 0:
        return jsonify({"predictions": [0.0] * months})
    if n == 1:
        return jsonify({"predictions": [float(values[0])] * months})

    X = np.arange(n).reshape(-1, 1)
    model = LinearRegression().fit(X, values)
    future = np.arange(n, n + months).reshape(-1, 1)
    preds = np.maximum(model.predict(future), 0)
    return jsonify({"predictions": [float(p) for p in preds]})


if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000)
