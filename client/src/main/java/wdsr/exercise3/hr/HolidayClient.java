package wdsr.exercise3.hr;

import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl;
import wdsr.exercise3.ws.*;

import javax.jws.WebParam;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.net.URL;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.logging.Logger;

// TODO Complete this class to book holidays by issuing a request to Human Resource web service.
// In order to see definition of the Human Resource web service:
// 1. Run HolidayServerApp.
// 2. Go to http://localhost:8090/holidayService/?wsdl
public class HolidayClient {

    private HumanResourceService humanResourceService;

    /**
     * Creates this object
     *
     * @param wsdlLocation URL of the Human Resource web service WSDL
     */
    public HolidayClient(URL wsdlLocation) {
        humanResourceService = new HumanResourceService(wsdlLocation);

    }

    /**
     * Sends a holiday request to the HumanResourceService.
     *
     * @param employeeId Employee ID
     * @param firstName  First name of employee
     * @param lastName   Last name of employee
     * @param startDate  First day of the requested holiday
     * @param endDate    Last day of the requested holiday
     * @return Identifier of the request, if accepted.
     * @throws ProcessingException if request processing fails.
     */
    public int bookHoliday(int employeeId, String firstName, String lastName, Date startDate, Date endDate) throws ProcessingException {

        HumanResource humanResource = humanResourceService.getHumanResourcePort();
        ObjectFactory factory = new ObjectFactory();
        EmployeeType employee = factory.createEmployeeType();
        employee.setNumber(employeeId);
        employee.setFirstName(firstName);
        employee.setLastName(lastName);
        HolidayType holiday = factory.createHolidayType();

        holiday.setEndDate(dateToGregorianXmlDate(endDate));
        holiday.setStartDate(dateToGregorianXmlDate(startDate));
        if (holiday.getEndDate() == null || holiday.getStartDate() == null) {
            throw new ProcessingException();
        }

        HolidayRequest request = factory.createHolidayRequest();
        request.setEmployee(employee);
        request.setHoliday(holiday);
        HolidayResponse response;
        try {
            response = humanResource.holiday(request);
        } catch (Exception ex) {
            throw new ProcessingException("Requested operation was failed");
        }

        return response.getRequestId();
    }

    private XMLGregorianCalendar dateToGregorianXmlDate(Date date) {
        GregorianCalendar c = new GregorianCalendar();
        c.setTime(date);
        XMLGregorianCalendar calendar = null;
        try {
            calendar = javax.xml.datatype.DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
        } catch (DatatypeConfigurationException e) {
            e.printStackTrace();
        }
        return calendar;
    }

}
