import pj.domain.Role
import pj.io.FileIO
import pj.xml.XMLtoDomain

for
    xml    <- FileIO.load("C:\\Users\\caba\\Personal\\mei\\tap\\tap-m1a-1060503-1170541-1180511-1191244\\files\\assessment\\ms01\\valid_agenda_01_in.xml")
    agenda <- XMLtoDomain.agenda(xml)
yield agenda
