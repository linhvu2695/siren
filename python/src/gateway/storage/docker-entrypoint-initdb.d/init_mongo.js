db.createUser(
    {
        user: "root",
        pwd: "password",
        roles: [
            {
                role: "readWrite",
                db: "videos"
            },
            {
                role: "readWrite",
                db: "mp3s"
            }
        ]
    }
)