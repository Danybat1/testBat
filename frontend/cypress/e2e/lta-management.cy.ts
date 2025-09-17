describe('LTA Management E2E Tests', () => {
  beforeEach(() => {
    // Intercept API calls to mock backend responses
    cy.intercept('POST', '/api/auth/login', {
      statusCode: 200,
      body: {
        token: 'mock-jwt-token',
        user: {
          id: 1,
          email: 'admin@freightops.com',
          role: 'ADMIN',
          firstName: 'Admin',
          lastName: 'User'
        }
      }
    }).as('login');

    cy.intercept('GET', '/api/lta/stats', {
      statusCode: 200,
      body: {
        totalLTAs: 25,
        pendingLTAs: 8,
        inTransitLTAs: 12,
        deliveredLTAs: 5
      }
    }).as('getStats');

    cy.intercept('POST', '/api/lta', {
      statusCode: 201,
      body: {
        id: 123,
        ltaNumber: 'LTA-2024-001',
        status: 'PENDING',
        shipperName: 'Test Shipper Inc.',
        shipperAddress: '123 Shipper St, Montreal, QC',
        shipperPhone: '+1-514-555-0101',
        consigneeName: 'Test Consignee Ltd.',
        consigneeAddress: '456 Consignee Ave, Toronto, ON',
        consigneePhone: '+1-416-555-0202',
        pickupDate: '2024-01-15',
        deliveryDate: '2024-01-20',
        weight: 1500.0,
        dimensions: '120x80x60 cm',
        description: 'Test shipment for E2E testing',
        specialInstructions: 'Handle with care - fragile items',
        createdAt: '2024-01-10T10:00:00Z',
        updatedAt: '2024-01-10T10:00:00Z'
      }
    }).as('createLTA');
  });

  it('should allow user to login and create a new LTA', () => {
    // Step 1: Visit login page
    cy.visit('/auth/login');
    cy.contains('Connexion à FreightOps').should('be.visible');

    // Step 2: Login with admin credentials
    cy.get('[data-cy="email-input"]').should('be.visible').type('admin@freightops.com');
    cy.get('[data-cy="password-input"]').type('admin123');
    cy.get('[data-cy="login-button"]').click();

    // Wait for login API call
    cy.wait('@login');

    // Step 3: Verify successful login and navigation to dashboard
    cy.url().should('not.include', '/auth/login');
    cy.contains('Tableau de Bord LTA').should('be.visible');

    // Wait for stats to load
    cy.wait('@getStats');

    // Step 4: Navigate to create LTA page
    cy.get('[data-cy="create-lta-button"]').click();
    cy.url().should('include', '/lta/create');
    cy.contains('Créer un Nouveau LTA').should('be.visible');

    // Step 5: Fill out the LTA form
    const testLTA = {
      shipperName: 'Test Shipper Inc.',
      shipperAddress: '123 Shipper St, Montreal, QC',
      shipperPhone: '+1-514-555-0101',
      consigneeName: 'Test Consignee Ltd.',
      consigneeAddress: '456 Consignee Ave, Toronto, ON',
      consigneePhone: '+1-416-555-0202',
      pickupDate: '2024-01-15',
      deliveryDate: '2024-01-20',
      weight: 1500,
      dimensions: '120x80x60 cm',
      description: 'Test shipment for E2E testing',
      specialInstructions: 'Handle with care - fragile items'
    };

    cy.fillLTAForm(testLTA);

    // Step 6: Submit the form
    cy.get('[data-cy="submit-button"]').click();

    // Wait for create API call
    cy.wait('@createLTA');

    // Step 7: Verify successful creation
    cy.contains('LTA créé avec succès').should('be.visible');
    cy.url().should('include', '/lta/list');
  });

  it('should display validation errors for invalid form data', () => {
    // Login first
    cy.loginAsAdmin();
    cy.wait('@login');
    cy.wait('@getStats');

    // Navigate to create LTA
    cy.visit('/lta/create');
    cy.contains('Créer un Nouveau LTA').should('be.visible');

    // Try to submit empty form
    cy.get('[data-cy="submit-button"]').click();

    // Verify validation errors appear
    cy.contains('Le nom de l\'expéditeur est requis').should('be.visible');
    cy.contains('L\'adresse de l\'expéditeur est requise').should('be.visible');
    cy.contains('Le nom du destinataire est requis').should('be.visible');
    cy.contains('L\'adresse du destinataire est requise').should('be.visible');
    cy.contains('La date de collecte est requise').should('be.visible');
    cy.contains('Le poids est requis').should('be.visible');
  });

  it('should allow user to view LTA list and navigate to details', () => {
    // Mock LTA list API
    cy.intercept('GET', '/api/lta?**', {
      statusCode: 200,
      body: {
        content: [
          {
            id: 1,
            ltaNumber: 'LTA-2024-001',
            status: 'PENDING',
            shipperName: 'Test Shipper Inc.',
            consigneeName: 'Test Consignee Ltd.',
            pickupDate: '2024-01-15',
            deliveryDate: '2024-01-20',
            weight: 1500.0,
            createdAt: '2024-01-10T10:00:00Z'
          }
        ],
        totalElements: 1,
        totalPages: 1,
        size: 10,
        number: 0
      }
    }).as('getLTAList');

    // Login and navigate to LTA list
    cy.loginAsAdmin();
    cy.wait('@login');
    cy.wait('@getStats');

    cy.visit('/lta/list');
    cy.wait('@getLTAList');

    // Verify LTA list is displayed
    cy.contains('Liste des LTA').should('be.visible');
    cy.contains('LTA-2024-001').should('be.visible');
    cy.contains('Test Shipper Inc.').should('be.visible');

    // Click on view details
    cy.get('[data-cy="view-details-1"]').click();
    cy.url().should('include', '/lta/1');
  });

  it('should handle logout correctly', () => {
    // Login first
    cy.loginAsAdmin();
    cy.wait('@login');
    cy.wait('@getStats');

    // Verify user is logged in
    cy.contains('Admin User').should('be.visible');

    // Click logout
    cy.get('[data-cy="user-menu"]').click();
    cy.get('[data-cy="logout-button"]').click();

    // Verify redirect to login page
    cy.url().should('include', '/auth/login');
    cy.contains('Connexion à FreightOps').should('be.visible');
  });

  it('should handle API errors gracefully', () => {
    // Mock login error
    cy.intercept('POST', '/api/auth/login', {
      statusCode: 401,
      body: { message: 'Invalid credentials' }
    }).as('loginError');

    cy.visit('/auth/login');
    cy.get('[data-cy="email-input"]').type('invalid@email.com');
    cy.get('[data-cy="password-input"]').type('wrongpassword');
    cy.get('[data-cy="login-button"]').click();

    cy.wait('@loginError');

    // Verify error message is displayed
    cy.contains('Erreur de connexion').should('be.visible');
  });
});
