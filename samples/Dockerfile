FROM continuumio/miniconda3:4.3.14

ADD . /code
WORKDIR /code

RUN pip install --upgrade pip
RUN pip install --default-timeout=1000 tensorflow
RUN pip install --default-timeout=1000 -r requirements.txt

CMD ["python", "app.py"]

