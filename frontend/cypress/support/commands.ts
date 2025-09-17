// ***********************************************
// This example commands.ts shows you how to
// create various custom commands and overwrite
// existing commands.
//
// For more comprehensive examples of custom
// commands please read more here:
// https://on.cypress.io/custom-commands
// ***********************************************

declare global {
  namespace Cypress {
    interface Chainable {
      /**
       * Custom command to login with demo credentials
       * @example cy.loginAsAdmin()
       */
      loginAsAdmin(): Chainable<void>;
      
      /**
       * Custom command to login with agent credentials
       * @example cy.loginAsAgent()
       */
      loginAsAgent(): Chainable<void>;
      
      /**
       * Custom command to login with finance credentials
       * @example cy.loginAsFinance()
       */
      loginAsFinance(): Chainable<void>;
      
      /**
       * Custom command to fill LTA form
       * @example cy.fillLTAForm(ltaData)
       */
      fillLTAForm(lta: any): Chainable<void>;
    }
  }
}

Cypress.Commands.add('loginAsAdmin', () => {
  cy.visit('/auth/login');
  cy.get('[data-cy="email-input"]').type('admin@freightops.com');
  cy.get('[data-cy="password-input"]').type('admin123');
  cy.get('[data-cy="login-button"]').click();
  cy.url().should('not.include', '/auth/login');
});

Cypress.Commands.add('loginAsAgent', () => {
  cy.visit('/auth/login');
  cy.get('[data-cy="email-input"]').type('agent@freightops.com');
  cy.get('[data-cy="password-input"]').type('agent123');
  cy.get('[data-cy="login-button"]').click();
  cy.url().should('not.include', '/auth/login');
});

Cypress.Commands.add('loginAsFinance', () => {
  cy.visit('/auth/login');
  cy.get('[data-cy="email-input"]').type('finance@freightops.com');
  cy.get('[data-cy="password-input"]').type('finance123');
  cy.get('[data-cy="login-button"]').click();
  cy.url().should('not.include', '/auth/login');
});

Cypress.Commands.add('fillLTAForm', (lta) => {
  cy.get('[data-cy="shipper-name"]').type(lta.shipperName);
  cy.get('[data-cy="shipper-address"]').type(lta.shipperAddress);
  cy.get('[data-cy="shipper-phone"]').type(lta.shipperPhone);
  cy.get('[data-cy="consignee-name"]').type(lta.consigneeName);
  cy.get('[data-cy="consignee-address"]').type(lta.consigneeAddress);
  cy.get('[data-cy="consignee-phone"]').type(lta.consigneePhone);
  cy.get('[data-cy="pickup-date"]').type(lta.pickupDate);
  cy.get('[data-cy="delivery-date"]').type(lta.deliveryDate);
  cy.get('[data-cy="weight"]').type(lta.weight.toString());
  cy.get('[data-cy="dimensions"]').type(lta.dimensions);
  cy.get('[data-cy="description"]').type(lta.description);
  cy.get('[data-cy="special-instructions"]').type(lta.specialInstructions);
});

export {};
