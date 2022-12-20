from flask import Flask, request
from flask_mysqldb import MySQL
import jwt, datetime, os, logging

logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(message)s')

server = Flask(__name__)
mysql = MySQL(server)

# config
server.config["MYSQL_HOST"] = os.environ.get("MYSQL_HOST")
server.config["MYSQL_USER"] = os.environ.get("MYSQL_USER")
server.config["MYSQL_PASSWORD"] = os.environ.get("MYSQL_PASSWORD")
server.config["MYSQL_DB"] = os.environ.get("MYSQL_DB")
server.config["MYSQL_PORT"] = int(os.environ.get("MYSQL_PORT"))

@server.route("/login", methods = ["POST"])
def login():
    auth = request.authorization
    if not auth:
        logging.error("Login error: missing headers authorization")
        return "Missing Credentials", 401

    # Check DB for username & password
    cur = mysql.connection.cursor()
    result = cur.execute(
        "SELECT email, password FROM user WHERE email=%s", (auth.username,)
    )

    if result > 0:
        user_row = cur.fetchone()
        email = user_row[0]
        password = user_row[1]
        if auth.username != email or auth.password != password:
            return "Invalid credentials", 401
        else:
            return createJWT(auth.username, os.environ.get("JWT_SECRET",), True)
    else:
        return "Invalid credentials", 401

def createJWT(username, secret, authz):
    return jwt.encode(
        payload={
            "username": username,
            "exp": datetime.datetime.now(tz=datetime.timezone.utc) + datetime.timedelta(days=1),
            "iat": datetime.datetime.utcnow(),
            "admin": authz
        },
        key=secret,
        algorithm="HS256"
    )

@server.route("/validate", methods=["POST"])
def validate():
    encoded_jwt = request.headers["Authorization"]

    if not encoded_jwt:
        logging.error("Validation error: missing headers authorization")
        return "Missing credentials", 401
    
    encoded_jwt = encoded_jwt.split(" ")[1]
    try:
        decoded = jwt.decode(
            encoded_jwt, os.environ.get("JWT_SECRET"), algorithms=["HS256"]
        )
    except:
        return "Not authorized", 403
    return decoded, 200


if __name__ == "__main__":
    server.run(host="0.0.0.0", port=5000)