# Copyright 2016 Google Inc. All rights reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

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
