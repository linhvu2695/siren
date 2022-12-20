from flask import Flask, request, send_file
from flask_pymongo import PyMongo
from auth import validate
from auth_svc import access
from storage import util
from bson.objectid import ObjectId

import os, gridfs, pika, json, logging

logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(message)s')

server = Flask(__name__)

mongo_video = PyMongo(server, uri="mongodb://host.minikube.internal:27017/videos")
mongo_mp3 = PyMongo(server, uri="mongodb://host.minikube.internal:27017/mp3s")

fs_video = gridfs.GridFS(mongo_video.db)
fs_mp3 = gridfs.GridFS(mongo_mp3.db)

connection = pika.BlockingConnection(pika.ConnectionParameters("rabbitmq"))
channel = connection.channel()

@server.route("/login", methods=["POST"])
def login():
    token, err = access.login(request)
    if err:
        logging.error("Login error: " + err[0])
        return err
    return token 

@server.route("/upload", methods=["POST"])
def upload():
    access, err = validate.token(request)
    if err:
        logging.error("Validate error: " + err[0])
        return err

    access = json.loads(access)
    if not access["admin"]:
        logging.error("User is not authorized for uploading")
        return "Not authorized", 401

    if len(request.files) != 1:
        logging.error("Illegal number of files: " + str(len(request.files)))
        return "Exactly 1 file required", 400
    for _, f in request.files.items():
        err = util.upload(f, fs_video, channel, access)
        if err:
            logging.error("Upload error: " + err[0])
            return err
    return "Success", 200
    
@server.route("/download", methods=["GET"])
def download():
    access, err = validate.token(request)
    if err:
        logging.error("Validate error: " + err[0])
        return err

    access = json.loads(access)
    if not access["admin"]:
        logging.error("User is not authorized for uploading")
        return "Not authorized", 401

    fid = request.args.get("fid")
    if not fid:
        logging.error("Download error: missing fid")
        return "fid is required", 400

    try:
        out = fs_mp3.get(ObjectId(fid))
        return send_file(out, download_name=f"{fid}.mp3")
    except Exception as e:
        logging.error("Download error: " + str(e))
        return "Internal Server Error", 500
        

if __name__ == "__main__":
    server.run(host="0.0.0.0", port=8080)
