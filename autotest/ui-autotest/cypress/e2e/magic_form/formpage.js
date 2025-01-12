import magicform from '../action/location_object.json'
export default class MagicPage{
    get title_page(){
        cy.wait(10000)
        return cy.get(magicform.formpage.form_button)
    }

    get create_form(){
        cy.wait(3000)
        return cy.get(magicform.formpage.create_form)
    }

    get title(){
        cy.wait(3000)
        return cy.get(magicform.formpage.title)
    }

    get longtext(){
        cy.wait(3000)
        return cy.get(magicform.formpage.longtext)
    }

    get select(){
        cy.wait(3000)
        return cy.get(magicform.formpage.select)
    }

    get multi_select(){
        cy.wait(3000)
        return cy.get(magicform.formpage.multi_select)
    }

    get number(){
        cy.wait(3000)
        return cy.get(magicform.formpage.number)
    }

    get date(){
        cy.wait(3000)
        return cy.get(magicform.formpage.date)
    }
    get date001(){
        cy.wait(3000)
        return cy.get(magicform.formpage.date001)
    }

    get attachment(){
        cy.wait(3000)
        return cy.get(magicform.formpage.attachment)
    }

    get member(){
        cy.wait(3000)
        return cy.get(magicform.formpage.member)
    }

    get member_1(){
        cy.wait(3000)
        return cy.get(magicform.formpage.member_1)
    }

    get rating(){
        cy.wait(3000)
        return cy.get(magicform.formpage.rating)
    }

    get URL(){
        cy.wait(3000)
        return cy.get(magicform.formpage.URL)
    }

    get phone(){
        cy.wait(3000)
        return cy.get(magicform.formpage.phone)

    }

    get email(){
        cy.wait(3000)
        return cy.get(magicform.formpage.email)
    }
    get form_submit(){
        cy.wait(3000)
        return cy.get(magicform.formpage.form_submit)
    }


    formmagic(title,longtext,number,date,URL,phone,email){
        this.title_page.click()
        cy.get(cy.contains('Create form from this view').click())

        if(title!==""){

            this.title.type(title)
        }
        if(longtext!==""){
            this.longtext.type(longtext)
        }
        this.select.check('optegZ7r89Wkw')
        this.multi_select.check()

        if(number!==""){
            this.number.type(number)
        }

        if(date!==""){
            this.date.type(date)
        }

        this.date001.click()

        //this.attachment.selectFile('D://kk//11.jpg', { force: true })

       // this.member.click()

       // cy.contains('pengjin').click({force: true})

        this.rating.click()

        if(URL!==""){
            this.URL.type(URL)
        }

        if(phone!==""){
            this.phone.type(phone)
        }

        if(email!==""){
            this.email.type(email)
        }
        cy.wait(20000).contains('Submit').click()
        }


    }

