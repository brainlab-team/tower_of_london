from Ball import Ball
from Stick import Stick

class Tower:
    def __init__(self):
        self.sticks = [Stick(length) for length in [3, 2, 1]]
        # self.sticks[0].add_ball("green")
        # self.sticks[0].add_ball("blue")
        # self.sticks[0].add_ball("red")

    def __init__(self, start, goal):
        self.sticks = [Stick(length) for length in [3, 2, 1]]
        self.set_tower(start)
        self.goal_tower = goal

    def set_tower(self, start_setup=None):
        self.sticks = [Stick(length) for length in [3, 2, 1]]
        for x in start_setup[0]:
            self.sticks[0].add_ball(x)
        for x in start_setup[1]:
            self.sticks[1].add_ball(x)
        for x in start_setup[2]:
            self.sticks[2].add_ball(x)
        # self.sticks[0].add_ball("green")
        # self.sticks[0].add_ball("blue")
        # self.sticks[0].add_ball("red")
        '''
        if stick1 is not None:
            for ball in stick1:
                self.sticks[0].add_ball(ball)
        if stick2 is not None:
            for ball in stick2:
                self.sticks[1].add_ball(ball)
        if stick3 is not None:
            for ball in stick3:
                self.sticks[2].add_ball(ball)
        '''

    def move_ball(self, stick1_index, stick2_index):
        stick1 = self.sticks[stick1_index]
        stick2 = self.sticks[stick2_index]
        ball = stick1.balls.pop()
        stick2.balls.append(ball)

    def can_move_ball(self, stick1_index, stick2_index):
        return stick2_index != stick1_index and\
            (len(self.sticks[stick1_index].balls) > 0 and 
             not self.sticks[stick2_index].is_full())

    def valid_moves(self):
        valid_moves_list = []
        for i in range(len(self.sticks)):
            for j in range(len(self.sticks)):
                if self.can_move_ball(i, j):
                    valid_moves_list.append((i, j))
        return valid_moves_list

    def check_success(self):
        return (self.sticks[0] == self.goal_tower[0] and 
                self.sticks[1] == self.goal_tower[1] and
                self.sticks[2] == self.goal_tower[2])
        cond1 = self.sticks[0] == ["red"]
        cond2 = self.sticks[1] == ["green", "blue"]
        cond3 = self.sticks[2] == []
        return cond1 and cond2 and cond3
        return (self.sticks[0] == self.goal_tower.sticks[0] and 
                self.sticks[1] == self.goal_tower.sticks[1] and 
                self.sticks[2] == self.goal_tower.sticks[2])

    def check_success_sequence(self, moves_list):
        for move in moves_list:
            self.move_ball(move[0], move[1])
        return self.check_success()