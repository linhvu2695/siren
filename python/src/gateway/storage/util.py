import pika, json, logging

logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(message)s')

def upload(f, fs, channel, access):
    # upload file to MongoDB
    try:
        fid = fs.put(f)
    except Exception as e:
        logging.error("Unable to store file in MongoDB")
        return "Internal Server Error", 500

    message = {
        "video_fid": str(fid),
        "mp3_fid": None,
        "username": access["username"]
    }

    # put message on the queue
    try:
        channel.basic_publish(
            exchange="",
            routing_key="video",
            body=json.dumps(message),
            properties=pika.BasicProperties(
                delivery_mode=pika.spec.PERSISTENT_DELIVERY_MODE
            )
        )
        logging.info(f"Video_fid: {message['video_fid']}. Upload message published")
    except Exception as e:
        # delete the file from MongoDB
        fs.delete(fid)
        logging.error("Unable to publish upload message: " + str(e) + ". File is removed from MongoDB")
        return "Internal Server Error", 500
        
