import pika, json, tempfile, os, logging
import moviepy.editor
from bson.objectid import ObjectId

logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(message)s')

def start(message, fs_videos, fs_mp3s, channel):
    message = json.loads(message)
    logging.info("Receive message: " + str(message))

    # access video contents
    out = fs_videos.get(ObjectId(message["video_fid"]))
    tf = tempfile.NamedTemporaryFile()
    tf.write(out.read())

    # convert video to audio
    audio = moviepy.editor.VideoFileClip(tf.name).audio
    tf.close()
    tf_path = tempfile.gettempdir() + f"/{message['video_fid']}.mp3"
    audio.write_audiofile(tf_path)
    logging.info(f"Video {message['video_fid']}: conversion completed")

    # store the audio in MongoDB
    f = open(tf_path, "rb")
    data = f.read()
    fid = fs_mp3s.put(data)
    f.close()
    os.remove(tf_path)
    logging.info(f"Mp3 {fid}: stored in MongoDB")

    # notify user
    message["mp3_fid"] = str(fid)
    try:
        channel.basic_publish(
            exchange="",
            routing_key=os.environ.get("MP3_QUEUE"),
            body=json.dumps(message),
            properties=pika.BasicProperties(
                delivery_mode=pika.spec.PERSISTENT_DELIVERY_MODE
            )
        )
    except Exception as e:
        fs_mp3s.delete(fid)
        logging.error("Unable to publish conversion message: " + str(e) + ". File is removed from MongoDB")
        return "Failed to publish message"



