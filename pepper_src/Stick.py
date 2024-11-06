from Ball import Ball

class Stick:
    def __init__(self, length):
        self.length = length
        self.balls = []

    def add_ball(self, color):
        self.balls.append(Ball(color))

    def is_full(self):
        return len(self.balls) == self.length

    def __eq__(self, other):
        if len(self.balls) != len(other):
            return False
        same = True
        for i in range(len(self.balls)):
            if self.balls[i].color != other[i]:
                same = False
        return same