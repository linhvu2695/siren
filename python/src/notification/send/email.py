import smtplib, os, json, logging
from email.message import EmailMessage

logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(message)s')

def notification(message):
    try: 
        logging.info("Receive message: " + str(message))
        message = json.loads(message)
        video_fid = message["video_fid"]
        mp3_fid = message["mp3_fid"]
        sender_address = os.environ.get("GMAIL_ADDRESS")
        sender_password = os.environ.get("GMAIL_PASSWORD")
        receiver_address = message["username"]

        msg = EmailMessage()
        msg.set_content(f"""
            Your video file_id {video_fid} has been converted.
            Mp3 file_id: {mp3_fid} is now ready!
        """)
        msg["Subject"] = "MP3 Download"
        msg["From"] = sender_address
        msg["To"] = receiver_address

        session = smtplib.SMTP("smtp.gmail.com", 587)
        session.starttls()
        session.login(sender_address, sender_password)
        session.send_message(msg, sender_address, receiver_address)
        session.quit()
        logging.info("Email dispatched success")

    except Exception as err:
        logging.error("Email dispatch error: " + str(err))