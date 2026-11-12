CREATE DATABASE IF NOT EXISTS qlst8 CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE qlst8;
CREATE TABLE `tblUser8` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `username` VARCHAR(255) NOT NULL,
  `password` VARCHAR(255) NOT NULL,
  `fullName` VARCHAR(255) NOT NULL,
  `phone` VARCHAR(20),
  `email` VARCHAR(255),
  `address` VARCHAR(255),
  `role` VARCHAR(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_tblUser8_username` (`username`)
);

CREATE TABLE `tblCustomer8` (
  `id` INT NOT NULL,
  `memberCard` VARCHAR(255),
  `loyaltyPoints` INT,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_tblCustomer8_memberCard` (`memberCard`),
  CONSTRAINT `fk_tblCustomer8_User8` FOREIGN KEY (`id`) REFERENCES `tblUser8` (`id`)
);

CREATE TABLE `tblStaff8` (
  `id` INT NOT NULL,
  `position` VARCHAR(255),
  `salaryRate` DECIMAL(15,2),
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_tblStaff8_User8` FOREIGN KEY (`id`) REFERENCES `tblUser8` (`id`)
);

CREATE TABLE `tblWarehouseStaff8` (
  `id` INT NOT NULL,
  `warehouseArea` VARCHAR(255),
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_tblWarehouseStaff8_Staff8` FOREIGN KEY (`id`) REFERENCES `tblStaff8` (`id`)
);

CREATE TABLE `tblSalesStaff8` (
  `id` INT NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_tblSalesStaff8_Staff8` FOREIGN KEY (`id`) REFERENCES `tblStaff8` (`id`)
);

CREATE TABLE `tblManager8` (
  `id` INT NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_tblManager8_Staff8` FOREIGN KEY (`id`) REFERENCES `tblStaff8` (`id`)
);

CREATE TABLE `tblDeliveryStaff8` (
  `id` INT NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_tblDeliveryStaff8_Staff8` FOREIGN KEY (`id`) REFERENCES `tblStaff8` (`id`)
);

CREATE TABLE `tblItem8` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `code` VARCHAR(50) NOT NULL,
  `name` VARCHAR(255) NOT NULL,
  `manufacturer` VARCHAR(255),
  `specs` VARCHAR(500),
  `price` DECIMAL(15,2) NOT NULL CHECK (`price` > 0),
  `stockQuantity` INT NOT NULL CHECK (`stockQuantity` >= 0),
  `warrantyPeriod` INT,
  `unit` VARCHAR(255),
  `updatedAt` DATETIME,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_tblItem8_code` (`code`)
);

CREATE TABLE `tblSupplier8` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(255) NOT NULL,
  `phone` VARCHAR(20),
  `email` VARCHAR(255),
  `address` VARCHAR(255),
  `description` VARCHAR(500),
  PRIMARY KEY (`id`)
);

CREATE TABLE `tblOnlineOrder8` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `orderDate` DATETIME,
  `status` VARCHAR(255) NOT NULL,
  `deliveryAddress` VARCHAR(255),
  `paymentMethod` VARCHAR(255) NOT NULL,
  `idCustomer8` INT NOT NULL,
  `idWarehouseStaff8` INT NULL,
  `idDeliveryStaff8` INT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_tblOnlineOrder8_Customer8` FOREIGN KEY (`idCustomer8`) REFERENCES `tblCustomer8` (`id`),
  CONSTRAINT `fk_tblOnlineOrder8_WarehouseStaff8` FOREIGN KEY (`idWarehouseStaff8`) REFERENCES `tblWarehouseStaff8` (`id`),
  CONSTRAINT `fk_tblOnlineOrder8_DeliveryStaff8` FOREIGN KEY (`idDeliveryStaff8`) REFERENCES `tblDeliveryStaff8` (`id`)
);

CREATE TABLE `tblOrderItem8` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `quantity` INT NOT NULL CHECK (`quantity` > 0),
  `price` DECIMAL(15,2) NOT NULL CHECK (`price` > 0),
  `idOnlineOrder8` INT NOT NULL,
  `idItem8` INT NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_tblOrderItem8_OnlineOrder8` FOREIGN KEY (`idOnlineOrder8`) REFERENCES `tblOnlineOrder8` (`id`),
  CONSTRAINT `fk_tblOrderItem8_Item8` FOREIGN KEY (`idItem8`) REFERENCES `tblItem8` (`id`)
);

CREATE TABLE `tblInvoice8` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `createdDate` DATETIME,
  `paymentMethod` VARCHAR(255) NOT NULL,
  `idSalesStaff8` INT NOT NULL,
  `idCustomer8` INT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_tblInvoice8_SalesStaff8` FOREIGN KEY (`idSalesStaff8`) REFERENCES `tblSalesStaff8` (`id`),
  CONSTRAINT `fk_tblInvoice8_Customer8` FOREIGN KEY (`idCustomer8`) REFERENCES `tblCustomer8` (`id`)
);

CREATE TABLE `tblInvoiceItem8` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `quantity` INT NOT NULL CHECK (`quantity` > 0),
  `price` DECIMAL(15,2) NOT NULL CHECK (`price` > 0),
  `idInvoice8` INT NOT NULL,
  `idItem8` INT NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_tblInvoiceItem8_Invoice8` FOREIGN KEY (`idInvoice8`) REFERENCES `tblInvoice8` (`id`),
  CONSTRAINT `fk_tblInvoiceItem8_Item8` FOREIGN KEY (`idItem8`) REFERENCES `tblItem8` (`id`)
);

CREATE TABLE `tblImportNote8` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `importDate` DATETIME,
  `note` VARCHAR(500),
  `idWarehouseStaff8` INT NOT NULL,
  `idSupplier8` INT NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_tblImportNote8_WarehouseStaff8` FOREIGN KEY (`idWarehouseStaff8`) REFERENCES `tblWarehouseStaff8` (`id`),
  CONSTRAINT `fk_tblImportNote8_Supplier8` FOREIGN KEY (`idSupplier8`) REFERENCES `tblSupplier8` (`id`)
);

CREATE TABLE `tblImportDetail8` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `quantity` INT NOT NULL CHECK (`quantity` > 0),
  `importPrice` DECIMAL(15,2) NOT NULL CHECK (`importPrice` > 0),
  `idImportNote8` INT NOT NULL,
  `idItem8` INT NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_tblImportDetail8_ImportNote8` FOREIGN KEY (`idImportNote8`) REFERENCES `tblImportNote8` (`id`),
  CONSTRAINT `fk_tblImportDetail8_Item8` FOREIGN KEY (`idItem8`) REFERENCES `tblItem8` (`id`)
);
