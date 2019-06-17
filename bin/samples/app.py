from flask import Flask, request
from flask_restful import Resource, Api
import jwt, base64
from datetime import datetime
from main import init, predict

app = Flask(__name__)
api = Api(app)


@app.route('/load_and_predict', methods=['GET', 'POST'])
def load_and_predict():
    token = request.headers.get('Authorization')
    time = datetime.today().timestamp()
    payload = jwt.decode(token[7:], base64.b64decode(app.config.get('SECRET_KEY')), algorithms='HS256')
    if payload['sub'] == app.config['user'] and payload['token_type'] == 'access_token' and payload['roles'] == app.config['role']:
        if request.method == 'POST':
            some_json = request.get_json()
            result = predict(some_json)
            return 'predict response: {}'.format(result)
        else:
            return payload
    else:
        return "invalid token"


init()

if __name__ == "__main__":
