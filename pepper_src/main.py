# -*- coding: utf-8 -*-

from BaseHTTPServer import BaseHTTPRequestHandler, HTTPServer
import json
import signal
import qi
import argparse
import time
import sys
import sqlite3  
from pepper_controller import PepperController
import database
import os
import subprocess
import json
database_file = 'pepperDatabase.db'
from Tower import Tower
import random

#Funzione che prende in input due configurazioni, una iniziale e una obiettivo, 
#e prevede le mosse da fare per passare dalla iniziale a quella obiettivo
def BFS(tower, start, goal):
    frontier = [[valid_move] for valid_move in tower.valid_moves()]

    while len(frontier) > 0:
        moves_list = frontier.pop(0)
        new_tower = Tower(start, goal)
        if new_tower.check_success_sequence(moves_list):
            print(moves_list)
            return moves_list
            break
        if len(moves_list) < 8:
            valid_moves_lst = new_tower.valid_moves()
            for valid_move in valid_moves_lst:
                new_tower_list = moves_list[:]
                new_tower_list.append(valid_move)
                frontier.append(new_tower_list)

# Classe per gestire le richieste HTTP POST
class RequestHandler(BaseHTTPRequestHandler):
    def __init__(self, request, client_address, server, pepper_controller=None):
        self.pepper_controller = pepper_controller 
        BaseHTTPRequestHandler.__init__(self, request, client_address, server)

    # Funzione per gestire il segnale di interruzione (Ctrl + C)
    def signal_handler(sig, frame):
        print('Stopping server...')
        sys.exit(0)

    # Associa la funzione di gestione del segnale al segnale di interruzione (SIGINT
    signal.signal(signal.SIGINT, signal_handler)
    
    def do_POST(self):
        content_length = int(self.headers['Content-Length'])
        post_data = self.rfile.read(content_length)
        data = json.loads(post_data.decode('utf-8'))
        
        positive = ["Ottimo, hai scelto il percorso ottimale.",
            "Bravissimo, hai optato per la via più efficace.",
            "Eccellente, hai selezionato la strada ottimale.",
            "Perfetto, hai fatto la scelta giusta.",
            "Magnifico, hai scelto la via più efficiente.",
            "Meraviglioso, hai preso la decisione ottimale.",
            "Grandioso, hai optato per il percorso migliore.",
            "Straordinario, hai fatto una scelta ottima.",
            "Eccezionale, hai selezionato la soluzione perfetta.",
            "Incredibile, hai scelto la strada più adatta."]

        good = ["Fantastico, ma cerca di ottimizzare le mosse.",
               "Eccellente lavoro, ma potresti ridurre il numero di mosse.",
               "Hai fatto un ottimo lavoro, prova a essere più efficiente con le mosse.",
               "Bravo, ma puoi cercare di fare meno mosse.",
               "Ottimo lavoro, ma cerca di minimizzare le mosse necessarie.",
               "Sei sulla strada giusta, ma cerca di ottimizzare il tuo approccio.",
               "Complimenti, ma potresti migliorare la tua efficienza nei movimenti.",
               "Hai fatto bene, ma cerca di ridurre il numero di mosse per migliorare ancora.",
               "Stai facendo progressi, ma prova a ridurre il numero di mosse richieste.",
               "Continua così, ma cerca di fare meno mosse possibili."]

        negative = ["Hai fatto un buon lavoro! Ricorda, hai sempre la possibilità di chiedere suggerimenti.",
                      "Bravo! Non dimenticare che puoi sempre chiedere aiuto quando ne hai bisogno.",
                      "Ottimo lavoro! Tieni presente che puoi chiedere suggerimenti in qualsiasi momento.",
                      "Complimenti! Non esitare a richiedere assistenza quando ti serve.",
                      "Fantastico! Ricorda che hai l'opzione di chiedere suggerimenti durante la modalità full support."]

        bad = ["Ottimo lavoro! Cerca semplicemente di essere un po' più veloce.",
                  "Hai fatto bene! Prova ad accelerare il tuo ritmo.",
                  "Bravo! Cerca di completare le azioni un po' più rapidamente.",
                  "Fantastico! Cerca di essere un po' più veloce nel tuo approccio.",
                  "Eccellente! Continua così, ma cerca di muoverti un po' più velocemente."]



        #Controllo sui dati ricevuti e analisi dei vari casi
        #Caso in cui si riceve i dati relativi alla partita
        if(data.get("result")):
            result = data.get('result')
            test = data.get('test')
            time = data.get('time')
            moves = data.get('moves')
            hints = data.get('hints') 
            modality = data.get('modality')
            userID = data.get('user')
            minimumMoves = data.get('minimumMoves')
            timeInt = 60 - int(time)
            print("Received data: result=%s, test=%s, time=%s, moves=%s hints=%s modality=%s, user=%s, minimumMoves=%s\n" % (result, test, timeInt, moves, hints, modality, userID, minimumMoves))

            if timeInt == 60:
                result = "false"

            #Modifiche della variabile result per rendere più leggibili i dati
            if result == "true":
                result = "Successo"
                if int(moves) == int(minimumMoves):
                    #Feedback da parte del robot Pepper rigurardante i risultati
                    #caso in cui il test è stato completato entro il tempo limite e il numero di mosse è ottimale
                    val = random.randint(0, 9)
                    self.pepper_controller.say_animated_text(positive[val])
                else:
                    #Feedback da parte del robot Pepper rigurardante i risultati caso
                    #caso in cui il test è stato completato entro il tempo limite ma il numero di mosse non è ottimale  
                    val = random.randint(0, 9)
                    self.pepper_controller.say_animated_text(good[val])
            else:
                result = "Fallimento"
                if int(modality) == 1:
                    #Feedback da parte del robot Pepper rigurardante i risultati
                    #caso in cui il test non è stato completato entro il tempo limite e ci troviamo in modalità full support
                    val = random.randint(0, 9)
                    self.pepper_controller.say_animated_text(negative[val])
                else:
                    #Feedback da parte del robot Pepper rigurardante i risultati
                    #caso in cui il test non è stato completato entro il tempo limite e ci troviamo in modalità fostering
                    val = random.randint(0, 9)
                    self.pepper_controller.say_animated_text(bad[val])

            #Calcolo del punteggio
            if timeInt < 0 or timeInt > 59:
                score = 0
            elif timeInt <= 15:
                score = 3
            elif timeInt <= 30:
                score = 2
            else:
                score = 1

            #Calcolo del test
            if int(modality) == 0:
                if int(test) == 1:
                    testID = 1
                else:
                    testID = 4
            elif int(modality) == 1:
                testID = 2
            else:
                testID = 3
                
            #Inseriamo i dati nel database
            database.insertPlay(timeInt, int(moves), result, score, hints, testID, int(userID), int(minimumMoves))
            #Scriviamo i dati nel file risultati.xlsx
            database.printGames()

            # Invia una risposta al client
            self.send_response(200)
            self.send_header('Content-type', 'text/plain')
            self.end_headers()
            self.wfile.write('Received data successfully'.encode('utf-8'))
        #Caso in cui si ricevono le configurazioni e il robot da un suggerimento relativo alla prossima mossa
        elif(data.get("stick1") or data.get("stick2") or data.get("stick3")):
            # Estrarre i colori dei dischi inviati
            stick1_colors = data.get('stick1')
            stick2_colors = data.get('stick2')
            stick3_colors = data.get('stick3')
            stickFinal1_colors = data.get('stickFinal1')
            stickFinal2_colors = data.get('stickFinal2')
            stickFinal3_colors = data.get('stickFinal3')

            print("Received data: stick1_colors=%s, stick2_colors=%s, stick3_colors=%s, goal1_colors=%s, goal2_colors=%s, goal3_colors=%s\n" %(stick1_colors, stick2_colors, stick3_colors, stickFinal1_colors, stickFinal2_colors, stickFinal3_colors))

            #Invertiamo gli array ricevuti per la fun BFS
            stick1_colors.reverse()
            stick2_colors.reverse()
            stick3_colors.reverse()
            stickFinal1_colors.reverse()
            stickFinal2_colors.reverse()
            stickFinal3_colors.reverse()

            print("Reversed data: stick1_colors=%s, stick2_colors=%s, stick3_colors=%s, goal1_colors=%s, goal2_colors=%s, goal3_colors=%s\n" %(stick1_colors, stick2_colors, stick3_colors, stickFinal1_colors, stickFinal2_colors, stickFinal3_colors))

            #Definiamo la configurazione iniziale
            start = [stick1_colors, stick2_colors, stick3_colors]
            #Definiamo la configurazione obiettivo
            goal = [stickFinal1_colors, stickFinal2_colors, stickFinal3_colors]
            #Definiamo la struttura
            tower_manual = Tower(start, goal)
            
            #Calcoliamo le mosse per passare dalla iniziale alla obiettivo
            moves = BFS(tower_manual, start, goal)

            #Salviamo solo la prima da fare
            first_move = moves[0]
            stick_from, stick_to = first_move

            #Individuiamo la pallina che sta in testa al bastoncino
            ball = start[stick_from][-1]

            #Definiamo il colore della pallina che sta in cima
            if ball == "red":
                ballColor = "rosso"
            elif ball == "green":
                ballColor = "verde"
            else:
                ballColor = "blu"
            
            #Definiamo il bastoncino di partenza
            if stick_from == 0:
                stick_from_text = "Bastoncino alto"
            elif stick_from == 1:
                stick_from_text = "Bastoncino medio"
            else:
                stick_from_text = "Bastoncino corto"

            #Definiamo il bastoncino di arrivo
            if stick_to == 0:
                stick_to_text = "Bastoncino alto"
            elif stick_to == 1:
                stick_to_text = "Bastoncino medio"
            else:
                stick_to_text = "Bastoncino corto"
        
            #Feedback del robot che suggerisce la prossima mossa
            print("Sposta la pallina di colore %s dal %s e posizionalo sul %s" % (ballColor, stick_from_text, stick_to_text))
            self.pepper_controller.say_animated_text("Sposta la pallina di colore %s dal %s e posizionalo sul %s" % (ballColor, stick_from_text, stick_to_text))
            
            # Invia una risposta al client
            self.send_response(200)
            self.send_header('Content-type', 'text/plain')
            self.end_headers()
            self.wfile.write('Received data successfully'.encode('utf-8'))
        elif(data.get("aviableHelp")):
            #Feedback nel robot quando ci troviamo nella modalità full support
            #ricorda che si può richiedere un suggerimento
            self.pepper_controller.say_animated_text("Ricordati che in questa fase puoi richiedere suggerimenti senza limiti.")
            print("Possibile aiuto")
        elif(data.get("consecutiveHint")):
            #Feedback nel robot quando ci troviamo nella modalità fostering
            #ricorda che non si possono richiedere suggerimentoi consecutivi
            self.pepper_controller.say_animated_text("Hai appena chiesto un suggerimento, prova a farlo anche da solo")
        else:
            #Feedback nel robot iniziale
            #spiega come funziona il test e le regole
            self.pepper_controller.say_animated_text("Lascia che ti spieghi il test, allora:")
            self.pepper_controller.say_animated_text("Il test prevede 3 palline di colore diverso e 3 bastoncini di diversa lunghezza.")
            self.pepper_controller.say_animated_text("L'obiettivo di questo test è che i partecipanti, partendo da una configurazione iniziale delle palline sui bastoncini, riescano a ricreare una configurazione alternativa seguendo tali regole:")
            self.pepper_controller.say_animated_text("è possibile muovere solo una pallina alla volta;")
            self.pepper_controller.say_animated_text("è possibile muovere una pallina solo se non ha altre palline sopra di essa;")
            self.pepper_controller.say_animated_text("è possibile collocare una sola pallina sul bastoncino piccolo, due sul bastoncino medio, tre sul bastoncino grande.")
            self.pepper_controller.say_animated_text("Adesso inizia a prendere un'pò confidenza col test e fai partire la fase baseline")

# Funzione per avviare il server
def run_server(host='0.0.0.0', port=8080, pepper_controller=None):
    server_address = (host, port)
    httpd = HTTPServer(server_address, lambda *args, **kwargs: RequestHandler(*args, pepper_controller=pepper_controller, **kwargs))
    print('Starting server on %s:%s' % server_address)
    httpd.serve_forever()
    
def main(session):
    controller = PepperController(session)
    controller.initialize()
    run_server(pepper_controller=controller)
    
if __name__ == "__main__":
    try:
        pepper_ip = raw_input("Inserisci l'ip di pepper: ")
        connection_url = "tcp://"+pepper_ip+":9559"
        app = qi.Application(url=connection_url)
        app.start()
        if not os.path.isfile(database_file):
            database.createDatabase()
    except RuntimeError:
        print("Can't connect to Naoqi.")
        sys.exit(1)
    main(app.session)

