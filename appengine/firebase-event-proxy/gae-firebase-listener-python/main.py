import os
import webapp2

IS_DEV = os.environ["SERVER_SOFTWARE"][:3] == "Dev"
allowed_users = set()
if IS_DEV:
    allowed_users.add("dev-instance")
else:
    # Add your Java App Engine proxy App Id here
    allowed_users.add("your-java-appengine-proxy-app-id")

class LoggingHandler(webapp2.RequestHandler):

    def post(self):
      user = self.request.headers.get('X-Appengine-Inbound-Appid', None)
      if user and user in allowed_users:
          firebaseSnapshot = self.request.params['fbSnapshot']
          print firebaseSnapshot
      else:
          print "Got unauthenticated user: %s" % user

app = webapp2.WSGIApplication([
    webapp2.Route('/log', LoggingHandler),
])
