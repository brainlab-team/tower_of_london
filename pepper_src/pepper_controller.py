# -*- coding: utf-8 -*-

import time
import database
import os
import math

class PepperController:
    def __init__(self, session):
        self.session = session
        self.initialized = False
       
    def initialize(self):
        if not self.initialized:
            self.dialog_service = self.session.service("ALDialog")
            self.text_to_speech = self.session.service("ALTextToSpeech")
            self.tablet_service = self.session.service("ALTabletService")
            self.posture_proxy = self.session.service("ALRobotPosture")
            self.memory_proxy = self.session.service("ALMemory")
            self.motion_proxy = self.session.service("ALMotion")
            self.speech_detection_proxy = self.session.service("ALSpeechRecognition")
            self.photo_capture_proxy = self.session.service("ALPhotoCapture")
            self.emotion_analysis_proxy = self.session.service("ALVoiceEmotionAnalysis")
            self.animated_speech_proxy = self.session.service("ALAnimatedSpeech")
            self.autonomous_life_proxy = self.session.service("ALAutonomousLife")
            self.led_proxy = self.session.service("ALLeds")
            #self.speech_detection_proxy.setLanguage("English")
            #self.dialog_service.setLanguage("English")
            self.text_to_speech.setParameter("pitchShift", 1.2)
            self.text_to_speech.setParameter("speed", 100)
            self.text_to_speech.setLanguage("Italian")
            self.initialized = True
            self.reset_position()
            self.tablet_service._openSettings()
            
        else:
            print("Services are already initialized.")

    def reset_position(self):
        # Disabilita il movimento del corpo
        self.posture_proxy.goToPosture("Stand", 1.0)
        self.moveHeadWithAngle()
        self.moveBodyWithAngle(0.0)
        self.autonomous_life_proxy.setState("interactive")
        #safeguard per farlo stare fermo

    def say_text(self, text):
        self.text_to_speech.say(text)

    def say_animated_text(self, text):
        self.animated_speech_proxy.say(text)  

    def moveHeadWithAngle(self):
        self.motion_proxy.setAngles(["HeadPitch", "HeadYaw"], [math.radians(0.0), math.radians(0.0)], 1.0)
    
    def moveBodyWithAngle(self, value):
        self.motion_proxy.setAngles(["HipPitch"], [value], 1.0)