/*
 * LabLink Diagnostics System Database Schema
 * This file creates all necessary tables for the application.
 * It is executed by DatabaseInit.java
 */

-- Use the 'backend' database, as specified in DBConfig.java
USE backend;

--
-- Table structure for table `accounts`
--
CREATE TABLE IF NOT EXISTS `accounts` (
  `account_id` INT PRIMARY KEY AUTO_INCREMENT,
  `full_name` VARCHAR(255) NOT NULL,
  `username` VARCHAR(100) NOT NULL UNIQUE,
  `password_hash` TEXT NULL,
  `email` VARCHAR(255),
  `contact_number` VARCHAR(50),
  `sex` VARCHAR(20),
  `role` VARCHAR(20) NOT NULL,
  `profile_picture` VARCHAR(400) NULL,
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
  `password` VARCHAR(255) NULL,
);

--
-- Table structure for table `patients`
--
CREATE TABLE IF NOT EXISTS `patients` (
  `patient_id` INT PRIMARY KEY AUTO_INCREMENT,
  `name` VARCHAR(255) NOT NULL,
  `sex` VARCHAR(20),
  `date_of_birth` DATE,
  `contact_number` VARCHAR(50),
  `email` VARCHAR(255),
  `address` TEXT,
  `blood_type` VARCHAR(20),
  `allergies` TEXT,
  `existing_conditions` TEXT,
  `registered_by` INT,
  `registered_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (`registered_by`) REFERENCES `accounts`(`account_id`) ON DELETE SET NULL
);

--
-- Table structure for table `test_templates`
--
CREATE TABLE IF NOT EXISTS `test_templates` (
  `id` INT PRIMARY KEY AUTO_INCREMENT,
  `name` VARCHAR(255) UNIQUE NOT NULL,
  `category` VARCHAR(255),
  `description` TEXT
);

--
-- Table structure for table `template_parameters`
--
CREATE TABLE IF NOT EXISTS `template_parameters` (
  `id` INT PRIMARY KEY AUTO_INCREMENT,
  `template_id` INT NOT NULL,
  `param_name` VARCHAR(255) NOT NULL,
  `units` VARCHAR(50),
  `reference_range` VARCHAR(100),
  `critical_values` TEXT,
  FOREIGN KEY(`template_id`) REFERENCES `test_templates`(`id`) ON DELETE CASCADE
);

--
-- Table structure for table `tests`
--
CREATE TABLE IF NOT EXISTS `tests` (
  `test_id` INT PRIMARY KEY AUTO_INCREMENT,
  `patient_id` INT NOT NULL,
  `test_name` VARCHAR(255),
  `category` VARCHAR(100),
  `sample_type` VARCHAR(100),
  `technician` VARCHAR(255),
  `status` VARCHAR(50) DEFAULT 'Pending',
  `remarks` TEXT,
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (`patient_id`) REFERENCES `patients`(`patient_id`) ON DELETE CASCADE
);

--
-- Table structure for table `parameters`
--
CREATE TABLE IF NOT EXISTS `parameters` (
  `parameter_id` INT PRIMARY KEY AUTO_INCREMENT,
  `test_id` INT NOT NULL,
  `parameter_name` VARCHAR(255) NOT NULL,
  `result_value` VARCHAR(255),
  `normal_range` VARCHAR(100),
  `units` VARCHAR(50),
  `interpretation` VARCHAR(255),
  FOREIGN KEY (`test_id`) REFERENCES `tests`(`test_id`) ON DELETE CASCADE
);

--
-- Table structure for table `activity_log`
--
CREATE TABLE IF NOT EXISTS `activity_log` (
  `log_id` INT PRIMARY KEY AUTO_INCREMENT,
  `account_id` INT NOT NULL,
  `user` VARCHAR(100) NOT NULL,
  `timestamp` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `action` VARCHAR(100) NOT NULL,
  `target_table` VARCHAR(100),
  `target_id` INT,
  FOREIGN KEY (`account_id`) REFERENCES `accounts`(`account_id`) ON DELETE SET NULL
);

--
-- Table structure for table `patients`
-- (Based on PatientDAO.java and Patient.java)
-- CORRECTED: Added registered_at column
--
CREATE TABLE IF NOT EXISTS `patients` (
  `patient_id` INT PRIMARY KEY AUTO_INCREMENT,
  `name` VARCHAR(255) NOT NULL,
  `sex` VARCHAR(20),
  `date_of_birth` DATE,
  `contact_number` VARCHAR(50),
  `email` VARCHAR(255),
  `address` TEXT,
  `blood_type` VARCHAR(20),
  `allergies` TEXT,
  `existing_conditions` TEXT,
  `registered_by` INT,
  `registered_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- <-- THIS LINE WAS MISSING/WRONG
  FOREIGN KEY (`registered_by`) REFERENCES `accounts`(`account_id`) ON DELETE SET NULL
);