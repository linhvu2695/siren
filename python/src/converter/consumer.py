import pika, os, sys, time
from pymongo import MongoClient
import gridfs, logging
from convert import to_mp3

logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(message)s')

def main():
    client = MongoClient("host.minikube.internal", 27017)
    db_videos = client.get_database("videos")
    db_mp3s = client.get_database("mp3s")

    fs_videos = gridfs.GridFS(db_videos)
    fs_mp3s = gridfs.GridFS(db_mp3s)

    connection = pika.BlockingConnection(pika.ConnectionParameters(host="rabbitmq"))
    channel = connection.channel()
    
    def callback(ch, method, properties, body):
        err = to_mp3.start(body, fs_videos, fs_mp3s, ch)
        if err:
            logging.error("Mp3 conversion fail: " + str(err))
            ch.basic_nack(delivery_tag=method.delivery_tag)
            return
        logging.info("Mp3 conversion success")
        ch.basic_ack(delivery_tag=method.delivery_tag)

    channel.basic_consume(
        queue=os.environ.get("VIDEO_QUEUE"),
        on_message_callback=callback
    )

    print("Waiting for messages. To exit press CTRL+C")

    channel.start_consuming()

if __name__ == "__main__":
    try:
        main()
    except KeyboardInterrupt:
        print("Interrupted")
        try:
            sys.exit(0)
        except SystemExit:
            os._exit(0)
