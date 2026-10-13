# Optional Python ML service

    python -m venv venv
    venv\Scripts\activate        (Windows)   |   source venv/bin/activate  (Mac/Linux)
    pip install -r requirements.txt
    python app.py

Then in the backend's application.properties set:
    forecast.ml.url=http://localhost:5000/predict
and restart Spring Boot. The Forecast page will show "Python ML service" as the model.
If this service is not running, the backend automatically falls back to its built-in Java regression.
