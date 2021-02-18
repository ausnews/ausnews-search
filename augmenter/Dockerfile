FROM python:3.8.0-slim
RUN apt-get update \
&& apt-get install gcc -y \
&& apt-get install -y --no-install-recommends git \
&& apt-get clean
COPY requirements.txt /app/requirements.txt
WORKDIR app
RUN pip install --user -r requirements.txt
COPY . /app
CMD ["python", "/app/twitter.py"]
