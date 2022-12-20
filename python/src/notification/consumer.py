import pika, sys, os, logging
from send import email

logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(message)s')

def main():
    connection = pika.BlockingConnection(pika.ConnectionParameters(host="rabbitmq"))
    channel = connection.channel()

    def callback(ch, method, properties, body):
        err = email.notification(body)
        if err:
            logging.error("Email sent fail: " + str(err))
            ch.basic_nack(delivery_tag=method.delivery_tag)
            return
        logging.info("Email sent success")
        ch.basic_ack(delivery_tag=method.delivery_tag)
    
    channel.basic_consume(
        queue=os.environ.get("MP3_QUEUE"),
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
