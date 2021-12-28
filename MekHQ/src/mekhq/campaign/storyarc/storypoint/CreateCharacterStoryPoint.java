/*
 * CreateCharacterStoryPoint.java
 *
 * Copyright (c) 2021 - The MegaMek Team. All Rights Reserved
 *
 * This file is part of MekHQ.
 *
 * MekHQ is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MekHQ is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MekHQ.  If not, see <http://www.gnu.org/licenses/>.
 */
package mekhq.campaign.storyarc.storypoint;

import megamek.common.options.IOption;
import megamek.common.options.IOptionGroup;
import mekhq.MekHqXmlSerializable;
import mekhq.MekHqXmlUtil;
import mekhq.campaign.Campaign;
import mekhq.campaign.force.Force;
import mekhq.campaign.personnel.Person;
import mekhq.campaign.personnel.PersonnelOptions;
import mekhq.campaign.personnel.enums.PersonnelRole;
import mekhq.campaign.personnel.enums.Phenotype;
import mekhq.campaign.storyarc.StoryPoint;
import mekhq.campaign.unit.Unit;
import mekhq.campaign.universe.Faction;
import mekhq.campaign.universe.Factions;
import mekhq.gui.dialog.CreateCharacterDialog;
import org.apache.logging.log4j.LogManager;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.PrintWriter;
import java.io.Serializable;
import java.text.ParseException;
import java.time.temporal.ChronoUnit;
import java.util.Enumeration;
import java.util.UUID;

public class CreateCharacterStoryPoint extends StoryPoint implements Serializable, MekHqXmlSerializable {

    /** how much XP does the player have to spend on the character **/
    int xpPool;

    /** Initial characteristics of the person we may want */
    private int rank;
    private int age;
    private PersonnelRole primaryRole;
    private String firstname;
    private String surname;
    private String bloodname;
    private String biography;
    private Faction faction;
    private boolean clan;
    private Phenotype phenotype;
    private boolean commander;
    private int edge;

    /**
     * The id of the person in the campaign. This will otherwise be set randomly. By setting it manually we can
     * reference it later.
     */
    private UUID personId;

    private String instructions;
    private boolean editOrigin;

    /** ids to assign person to unit and force **/
    private UUID assignedUnitId;
    private int assignedForceId;


    public CreateCharacterStoryPoint() {
        super();
        firstname = "Bob";
        surname = "";
        bloodname = "";
        biography = "";
        commander = true;
        clan = false;
        phenotype = Phenotype.NONE;
        primaryRole = PersonnelRole.MECHWARRIOR;

        editOrigin = false;
    }

    @Override
    public String getTitle() {
        return "temp";
    }

    @Override
    protected String getResult() {
        return null;
    }

    public Person createPerson() {
        if(null == faction) {
            faction = getCampaign().getFaction();
        }
        Person p = new Person(getCampaign(), faction.getShortName());
        if(null != primaryRole) {
            p.setPrimaryRole(getCampaign(), primaryRole);
        }
        p.setClanner(clan);
        if(p.isClanner() && null != phenotype) {
            p.setPhenotype(phenotype);
        }

        p.setCommander(commander);
        p.setGivenName(firstname);
        p.setSurname(surname);
        p.setBloodname(bloodname);
        p.setBiography(biography);
        p.setRank(rank);
        if(edge > 0) {
            p.changeEdge(edge);
            setEdgeTriggers(p);
        }

        if(null != personId) {
            p.setId(personId);
        }

        p.setBirthday(getCampaign().getLocalDate().minus(age, ChronoUnit.YEARS));

        return p;
    }

    @Override
    public void start() {
        super.start();
        Person person = createPerson();
        final CreateCharacterDialog personDialog = new CreateCharacterDialog(null, true, person, getCampaign(), xpPool, instructions, editOrigin);
        getCampaign().importPerson(person);
        if(null != assignedUnitId) {
            Unit u = getCampaign().getUnit(assignedUnitId);
            if (null != u && u.isUnmanned()) {
                u.addPilotOrSoldier(person, false);
                //only assign to force if properly assigned to a unit
                Force force = getCampaign().getForce(assignedForceId);
                if(null != force && null != person.getUnit()) {
                    getCampaign().addUnitToForce(u, force.getId());
                }
            }
        }
        personDialog.setVisible(true);
        complete();
    }

    private void setEdgeTriggers(Person p) {
        //just check them all to be sure - no good way to separate these by primary role at the moment
        PersonnelOptions options = p.getOptions();

        for (Enumeration<IOptionGroup> i = options.getGroups(); i
                .hasMoreElements();) {
            IOptionGroup group = i.nextElement();

            if (!group.getKey().equalsIgnoreCase(PersonnelOptions.EDGE_ADVANTAGES)) {
                continue;
            }

            IOption option;
            for (Enumeration<IOption> j = group.getOptions(); j.hasMoreElements();) {
                option = j.nextElement();
                if(null != option && option.getType() == IOption.BOOLEAN) {
                    p.setEdgeTrigger(option.getName(), true);
                }
            }
        }
    }

    @Override
    public void writeToXml(PrintWriter pw1, int indent) {
        writeToXmlBegin(pw1, indent);

        String level = MekHqXmlUtil.indentStr(indent),
                level1 = MekHqXmlUtil.indentStr(indent + 1);

        StringBuilder builder = new StringBuilder(256);
        builder.append(level1)
                .append("<xpPool>")
                .append(xpPool)
                .append("</xpPool>")
                .append(NL)
                .append(level1)
                .append("<age>")
                .append(age)
                .append("</age>")
                .append(NL)
                .append(level1)
                .append("<rank>")
                .append(rank)
                .append("</rank>")
                .append(NL)
                .append(level1)
                .append("<firstname>")
                .append(firstname)
                .append("</firstname>")
                .append(NL)
                .append(level1)
                .append("<surname>")
                .append(surname)
                .append("</surname>")
                .append(NL)
                .append(level1)
                .append("<bloodname>")
                .append(bloodname)
                .append("</bloodname>")
                .append(NL)
                .append(level1)
                .append("<bloodname>")
                .append(bloodname)
                .append("</bloodname>")
                .append(NL)
                .append(level1)
                .append("<biography>")
                .append(biography)
                .append("</biography>")
                .append(NL)
                .append(level1)
                .append("<clan>")
                .append(clan)
                .append("</clan>")
                .append(NL)
                .append(level1)
                .append("<commander>")
                .append(commander)
                .append("</commander>")
                .append(NL)
                .append(level1)
                .append("<editOrigin>")
                .append(editOrigin)
                .append("</editOrigin>")
                .append(NL)
                .append(level1)
                .append("<instructions>")
                .append(instructions)
                .append("</instructions>")
                .append(NL)
                .append(level1)
                .append("<edge>")
                .append(edge)
                .append("</edge>")
                .append(NL)
                .append(level1)
                .append("<primaryRole>")
                .append(primaryRole.name())
                .append("</primaryRole>")
                .append(NL)
                .append(level1)
                .append("<phenotype>")
                .append(phenotype.name())
                .append("</phenotype>")
                .append(NL)
                .append(level1)
                .append("<assignedForceId>")
                .append(assignedForceId)
                .append("</assignedForceId>")
                .append(NL);;

        if(null != assignedUnitId) {
            builder.append(level1)
                    .append("<assignedUnitId>")
                    .append(assignedUnitId)
                    .append("</assignedUnitId>")
                    .append(NL);
        }
        if(null != faction) {
            builder.append(level1)
                    .append("<faction>")
                    .append(faction.getShortName())
                    .append("</faction>")
                    .append(NL);
        }
        if(null != instructions) {
            builder.append(level1)
                    .append("<instructions>")
                    .append(instructions)
                    .append("</instructions>")
                    .append(NL);
        }

        pw1.print(builder.toString());

        writeToXmlEnd(pw1, indent);
    }

    @Override
    protected void loadFieldsFromXmlNode(Node wn, Campaign c) throws ParseException {
        NodeList nl = wn.getChildNodes();

        for (int x = 0; x < nl.getLength(); x++) {
            Node wn2 = nl.item(x);

            try {
                if (wn2.getNodeName().equalsIgnoreCase("xpPool")) {
                    xpPool = Integer.parseInt(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("age")) {
                    age = Integer.parseInt(wn2.getTextContent().trim());
                }  else if (wn2.getNodeName().equalsIgnoreCase("rank")) {
                    rank = Integer.parseInt(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("firstname")) {
                    firstname = wn2.getTextContent().trim();
                } else if (wn2.getNodeName().equalsIgnoreCase("surname")) {
                    surname = wn2.getTextContent().trim();
                } else if (wn2.getNodeName().equalsIgnoreCase("bloodname")) {
                    bloodname = wn2.getTextContent().trim();
                } else if (wn2.getNodeName().equalsIgnoreCase("biography")) {
                    biography = wn2.getTextContent().trim();
                } else if (wn2.getNodeName().equalsIgnoreCase("clan")) {
                    clan = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("commander")) {
                    commander = Boolean.parseBoolean(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("primaryRole")) {
                    primaryRole = PersonnelRole.parseFromString(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("phenotype")) {
                    phenotype = Phenotype.parseFromString(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("faction")) {
                    faction = Factions.getInstance().getFaction(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("editOrigin")) {
                    editOrigin = Boolean.parseBoolean(wn2.getTextContent().trim());
                }  else if (wn2.getNodeName().equalsIgnoreCase("instructions")) {
                    instructions = wn2.getTextContent().trim();
                } else if (wn2.getNodeName().equalsIgnoreCase("assignedUnitId")) {
                    assignedUnitId = UUID.fromString(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("assignedForceId")) {
                    assignedForceId = Integer.parseInt(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("edge")) {
                    edge = Integer.parseInt(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("personId")) {
                    personId = UUID.fromString(wn2.getTextContent().trim());
                }
            } catch (Exception e) {
                LogManager.getLogger().error(e);
            }
        }
    }
}
