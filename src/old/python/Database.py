from sqlite3 import connect

class Database:

    def __init__(self, db_name):

        self.db_name = db_name

    def execute(self, cmd, extras=None):

        with connect(self.db_name) as db:

            cursor = db.cursor()

            if extras is None:
                cursor.execute(cmd)
            else:
                cursor.execute(cmd, extras)

            db.commit()

            return cursor.fetchall()
