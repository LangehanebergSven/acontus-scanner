About this app for development:


Ich hab ein existierendes ERP System, wo die Warenwirtschaft weiter ausgebaut werden soll:

Ich möchte eine Android App (ab Android 14) bauen, die als Barcode Scanner funktioniert und entsprechend Artikel Zu- und Abgänge sowie Material Zu- und Abgänge dokumentiert. Das ganze läuft nachher auf mein Keyence Gerät (BT-A600).

Ziel
- Die App ermöglicht eine schnelle, fehlerarme mobile Erfassung von Waren- und Materialbewegungen direkt am Lagerort mittels Keyence-Scanner.
  Nicht-Ziel
- Keine Inventur-App (falls zutreffend)
- Keine ERP-Administration
- Keine Benutzerverwaltung
- Keine Stammdatenänderungen
- Es gibt erstmal keine Unterscheidung der Nutzer

Alle Artikel/Materialien können (müssen nicht) einen Barcode haben, der die EAN beinhaltet. Diese EAN ist meistens im ERP System gepflegt und entsprechend ein schneller Weg um den Artikel auszuwählen.

Der gesamte Wareneingangs/Ausgangs Prozess auf der Android App soll wie folgt aussehen:

1. Der Mitarbeiter meldet sich in der App mit seiner Personal-Nr. an ohne Passwort. Die Personal-Nr. steht in der Datenbank und daraus können Mitarbeiterinformationen wie zum Beispiel Name ausgelesen.
   1.1 Nach erfolgreicher Anmeldung soll der App-Nutzer sehen könnnen, wer gerade angemeldet ist um nicht ausversehen als andere Person den Prozess zu verbuchen.
2. Der Scan Prozess soll wie folgt aussehen:
   2.1 Der Nutzer kann sich konfigurieren, welchem Buchungsgrund und welchem Lager die zu folglich scannenden Artikel/Materialien zuzuordnen sind.
   2.1.1 Der Buchungsgrund ist einer Tabelle in der existierenden ERP Datenbank zu entnehmen. Beispielhafte Gründe: Verzehr (Ausgang), Beschädigte Ware (Ausgang), Verdorbene Ware (Ausgang), Probe (Ausgang), Naturalrabatt (Ausgang), Produktionseingang (Eingang), Materialzukauf (Eingang, bezieht sich auf Material, nicht Artikel -> wichtig für die Freihand suche nachher), Materialabgang (Ausgang, bezieht sich auf Material -> wichtig für die Freihand suche nachher)
   2.1.2 An Buchungsgrund ist entsprechend für das System erkennbar, ob es Warenzugang oder Abgang ist und auch ob es Artikel oder Material ist.
   2.2 Sobald der Prozess gestartet wurde, landet der Nutzer erstmal auf einer leeren Übersicht der bereits gescannten Artikel (leer, weil der Prozess ja jetzt neu angelegt ist)
   2.3 In dieser Übersicht kann der Nutzer entweder weitere Konfigurationen hinzufügen oder die aktuelle Anpassen
   2.3.1 Zu den Konfigurationen gehört:
   - Lager darf abgeändert werden (für ab dann alle neu hinzugefügten Artikel/Materialien)
   - Buchungsgrund darf abgeändert werden (für ab dann alle neu hinzugefügten Artikel/Materialien)
   - MHD (Mindesthaltbarkeitsdatum) ist optional und darf hinterlegt werden (für ab dann alle neu hinzugefügten Artikel/Materialien)
   - Chargen-Nr. ist optional und darf hinterlegt werden (für ab dann alle neu hinzugefügten Artikel/Materialien)
   2.3.2 D.h.: Die Konfigurationen für bereits hinzufügte Artikel verändert sich nicht, wenn man die Konfiguration während des Prozesses ändert
   2.4 Mit den gewählten Konfigurationen, kann der Nutzer nun einzelne Artikel/Materialien scannen
   2.4.1 Für jeden Artikel muss nach dem Scan eine Menge und eine Inhaltsmenge in Zahlen angegeben werden. Inhaltsmenge ist optional. Die aktuelle Konfiguration wird an diesem Eintrag dann hinterlegt.
   2.4.2 Alternativ zum Scannen, soll der Nutzer auch per Suchfeld Artikel oder Materialien finden können (Name oder Nummer)
   2.4.2 Falls man den selben Artikel mehrfach scannt und eine Menge angibt, soll, falls mit identischer Konfiguration und gewählten Artikel bereits ein Eintrag vorhanden ist, die Menge erhöht werden statt einen neuen Eintrag zu erzeugen. Es soll visuell erkennbar sein durch ein kurzes leuchten welcher Eintrag neu ist / bearbeitet wurde.
   2.5 Nach und Nach füllt sich die Übersicht mit Artikeln.
   2.5.1 Man soll in der Übersicht per Multi-select Einträge wählen können und die gewählten Artikel in ihrer Konfiguration anpassen können (also MHD, Charge, Lager, Buchungsgrund)
   2.5.1.1 Während man im Multi-select ist, soll man auch alle Einträge aufeinmal makieren können
   2.5.1.2 Wenn man mehrer Aritkel mit unterschiedlichen Lagern bzw Buchungsgründen gewählt hat und man dabei ist dies zu Überschreiben, soll der Nutzer hier gewarnt werden, dass er dies gerade tut.
   2.5.2 Man soll in der Übersicht einzelne Einträge in ihrer Menge (und Inhaltsmenge) verändern können
   2.5.3 Man soll in der Übersicht einzelne Einträge wieder entfernen können
   2.5.4 Bei jedem Eintrag ist kompakt zu sehen: Der Artikel, die Konfiguration, der Zeitstempel
   2.6 Wenn der Nutzer meint, dass alle Einträge vollständig sind, soll er alles auf einmal zum ERP System übertragen können. Dies ist technisch nacher eine Menge von Inserts in eine Tabelle in der ERP Datenbank
   2.7 Der Nutzer soll sich abmelden können.
   2.8 Der aktuelle Prozess (Konfigurationen & hinzugefügte Einträge) sollen immer Lokal aufm Handy dem Nutzer (per PersonalNr) zugeordnet abgespeichert werden. Damit wenn man zwischendurch mal den Nutzer wechselt oder die App ausversehen schließt, die Daten noch vorhanden sind.


Technisches:
- Android 14 ist die minimum Android Version
- Von keyence gibt es eine SDK die für den Scann-Prozess hilft: com.keyence.autoid.sdk
- Die Datenbankaufrufe dürfen direkt aus der App passieren, auch wenn dies eine Sicherheitslücke ist, weil die Connection in der App liegt. Die App wird nicht im Playstore gelangen und die Datenbank ist auf einem lokalen Server.
- Es soll eine DatabaseConnector Klasse geben, die die SQL Anfragen loggt. In den Einstellungen der App, soll es die Möglichkeit geben alles geloggte zur Datenbank zu übertragen, unter der Annahme, dass die Verbindung wieder existiert.
- Sämtliche Read-Requests dürfen gecached werden
    - Der Cache hält 48 Std
    - Der Cache darf invalidiert werden über die Einstellungen in der App
    - Im Cache enthalten sind dann z.B. sämtliche (für die App relevante) Artikeldaten, Lagerdaten, Konfigurationsdaten (Buchungsgründe, Lager), Personaldaten...
    - Der Cache gilt global
- In sämtlichen Vorgängen soll bei Fehlern ein Dialogfenster aufgehen, wo aktuell ein Problem liegt (z.b. Artikel mit gescannten EAN nicht gefunden)
- Falls die Datenbank nicht erreichbar ist, soll man trotzdem weiterarbeiten können mithilfe der gecachten Daten.
- Wenn beim Übertragen des Prozesses (alle gescannten Einträge) die Datenbank nicht erreichbar ist, soll dem Nutzer angezeigt werden, dass die Übertragung gerade nicht funktioniert hat, dass die Daten zwischengespeichert sind für ihn, auch wenn er sich abmeldet.
- Das Datenmodell soll später vom KI-Agenten bestmöglich selbst gewählt werden. In einer Repository-Abstraktionsschicht zur Datenbank werde ich händisch das Mapping vornehmen.
- Die App-Code Architektur soll dem Best-Practice für Java Android Apps entsprechen für diesen Scope.
- Es gibt keine Performance-Anforderungen
- Da Daten zwischengespeichert werden pro Nutzer, können 2 Nutzer am selben Gerät arbeiten
- Teilübertragung nicht möglich
- Der Implementierungscode soll englisch sein, aber die UI Texte deutsch.