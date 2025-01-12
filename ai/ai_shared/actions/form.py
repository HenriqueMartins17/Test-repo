from .base import ServerRequestAction

# class RequestClientShowFormFunction(OpenAIFunction):
#     def call(self):
#         # send_action(FormAction)
#         pass


class FormAction(ServerRequestAction):
    name = "form"
    id: str = ""
    form_id: str

    def call(self, parameters):
        # ui.showForm()
        pass


# class FormSubmitFunction(OpenAIFunction):
#     def call(self):
#         # form_submit
#         pass


# class FormSubmitAction(FormSubmitFunction):
#     def call(self):
#         # databus.form_submit
#         pass
