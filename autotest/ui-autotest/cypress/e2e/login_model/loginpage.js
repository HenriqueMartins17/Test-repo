import location from '../action/location_object.json'
export default class LoginPage {

    constructor() {
        cy.wait(20000)
        this.url = 'https://vika.cn/login'
    }

    get password_button() {
        cy.wait(20000)
        return cy.get(location.loginpage.password_login)

    }

    get username() {
        cy.wait(20000)
        return cy.get(location.loginpage.username)

    }

    get password() {
        cy.wait(20000)
        return cy.get(location.loginpage.password)

    }

    get submit() {
        cy.wait(20000)
        return cy.get(location.loginpage.submit)

    }

    visit() {
        cy.wait(20000)
        cy.visit(this.url)
    }

//Encapsulate login business flow
    login(name,pwd){
        cy.wait(20000)
        this.password_button.click()

        if(name!==""){


            this.username.type(name)
        }
        if(pwd!==""){
            this.password.type(pwd)
        }
        this.submit.click()
    }

}