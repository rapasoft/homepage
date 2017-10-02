# Lazy loaded dialogs with PrimeFaces
- Rapasoft
- 
- 2014/12/21
- PrimeFaces
- publish

In an enterprise application it is a good practice to notify user about long running background tasks. Usual scenarios include filling out some forms and submitting them to server for background computation. While waiting for the result there is a notification displayed about current operation.

In PrimeFaces (5.1) this can be done easily by intercepting global ajax calls via ``<p:ajaxStatus />`` component together with ``<p:dialog />``. When the form is submitted via ajax the ajaxStatus can display spinner or waiting dialog. 

But let's be more creative. Imagine you need to display results of difficult computation in another dialog. Since dialogs are by default rendered during page load you can use ``dynamic='true'`` option which will load the content of the dialog lazily when they are first shown. This code snippets illustrates state we want to achieve:

    <h:body>
    
		<!-- Status dialog that is controlled by ajaxStatus component -->
        <p:dialog widgetVar="statusDialog" modal="true" showHeader="false" resizable="false">
            Please wait...
        </p:dialog>
    
		<!-- Define actions for starting and completing ajax request (show/hide dialog)
        <p:ajaxStatus onstart="PF('statusDialog').show()" oncomplete="PF('statusDialog').hide()"/>
    
		<!-- Display the results of a long running task, but only when user requested it -->
        <p:dialog widgetVar="meaningOfLifeDialog" dynamic="true">
            <h:outputText value="#{philosophyBean.meaningOfLife}"/>
        </p:dialog>
    
        <p:commandButton onclick="PF('meaningOfLifeDialog').show();" value="Click me for meaning of life"/>
    
    </h:body>
    
The backing bean is really simple:

    @ManagedBean
    @RequestScoped
    public class PhilosophyBean implements Serializable {
    
        public String getMeaningOfLife() throws InterruptedException {
            Thread.sleep(3000); // Here is something that takes few seconds to open
            return "It's 42, duh!";
        }
    
    }

As a result, when user clicks on "Meaning of life" button a dialog is shown with the "Please wait" message. After the request is processed the result is displayed in a separate ``meaningOfLifeDialog``.