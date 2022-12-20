import os, requests

def login(request):
    auth = request.authorization
    if not auth:
        return None, ("Missing credentials", 401)
    basicAuth = (auth.username, auth.password)

    respone = requests.post(
        f"http://{os.environ.get('AUTH_SVC_ADDRESS')}/login",
        auth=basicAuth
    )

    if respone.status_code == 200:
        return respone.text, None
    else:
        return None, (respone.text, respone.status_code)